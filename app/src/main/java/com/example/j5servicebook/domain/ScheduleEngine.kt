package com.example.j5servicebook.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

enum class Status { OK, DUE_SOON, OVERDUE }

/** Which limit is reached first: the date limit or the km limit. */
enum class Limit { DATE, KM }

enum class WarrantyStatus { ACTIVE, ENDING_SOON, EXPIRED }

data class Odometer(val km: Int, val date: LocalDate)

data class PeriodicService(
    val number: Int,
    val cyclePosition: Int,
    val dueDate: LocalDate,
    val dueKm: Int,
    val effectiveDate: LocalDate,
    val limit: Limit,
    val expectedKm: Int,
    val airFilter: Code,
    val acFilter: Code,
    val status: Status,
    val daysLeft: Long,
    val kmLeft: Int,
)

data class IntervalDue(
    val rule: IntervalRule,
    val lastDate: LocalDate,
    val lastKm: Int,
    val doneBefore: Boolean,
    val dueDate: LocalDate?,
    val dueKm: Int,
    val effectiveDate: LocalDate,
    val limit: Limit,
    val progress: Double,
    val status: Status,
    val daysLeft: Long,
    val kmLeft: Int,
)

data class WarrantyState(
    val name: String,
    val category: WarrantyCategory,
    val startDate: LocalDate,
    val startKm: Int,
    val expiryDate: LocalDate,
    val limitKm: Int,
    val effectiveEnd: LocalDate,
    val limit: Limit,
    val progress: Double,
    val status: WarrantyStatus,
    val daysLeft: Long,
    val kmLeft: Int,
    val note: String? = null,
)

data class VisitItem(val item: ItemId, val label: String, val action: String, val replace: Boolean)

/**
 * Pure schedule calculator. Every function that depends on the current day takes [today]
 * so results are reproducible in tests.
 */
class ScheduleEngine(
    val purchaseDate: LocalDate,
    readings: List<Reading>,
    records: List<ServiceRecord>,
) {
    /** Oldest first. */
    val records: List<ServiceRecord> = records.sortedWith(compareBy({ it.date }, { it.km }, { it.id }))

    private val points: List<Odometer> = buildList {
        add(Odometer(0, purchaseDate))
        readings.forEach { add(Odometer(it.km, it.date)) }
        records.forEach { add(Odometer(it.km, it.date)) }
    }

    /** Highest odometer value across readings and service records. */
    val latest: Odometer = points.maxWith(compareBy({ it.km }, { it.date }))

    // Driving rate kept as a fraction (km over days) so projections are exact.
    private val rateKm: Long
    private val rateDays: Long

    init {
        val (k, d) = drivingRate()
        rateKm = k
        rateDays = d
    }

    val kmPerDay: Double get() = rateKm.toDouble() / rateDays

    private fun drivingRate(): Pair<Long, Long> {
        val total = days(purchaseDate, latest.date)
        if (total > RATE_WINDOW_DAYS) {
            // Prefer recent driving once there is enough history.
            val from = latest.date.minusDays(RATE_WINDOW_DAYS)
            val to = latest.date.minusDays(RATE_MIN_SPAN_DAYS)
            val anchor = points
                .filter { it.date >= from && it.date <= to && it.km <= latest.km }
                .minByOrNull { it.date }
            if (anchor != null) {
                return atLeastOnePerDay((latest.km - anchor.km).toLong(), days(anchor.date, latest.date))
            }
        }
        return atLeastOnePerDay(latest.km.toLong(), max(1L, total))
    }

    private fun atLeastOnePerDay(km: Long, days: Long): Pair<Long, Long> =
        if (km < days) 1L to 1L else km to days

    /** Projected date the odometer reaches [km]; today if it already has. Never before today. */
    fun dateForKm(km: Int, today: LocalDate): LocalDate {
        if (km <= latest.km) return today
        val delta = (km - latest.km).toLong()
        val daysNeeded = (delta * rateDays + rateKm - 1) / rateKm
        return maxOf(latest.date.plusDays(daysNeeded), today)
    }

    /** Projected odometer value on [date]. */
    fun kmAtDate(date: LocalDate): Int {
        val d = days(latest.date, date)
        if (d <= 0) return latest.km
        return latest.km + ((d * rateKm + rateDays / 2) / rateDays).toInt()
    }

    val periodicCount: Int = this.records.count { it.kind == ServiceKind.PERIODIC }

    private val periodicBase: Odometer = this.records.lastOrNull { it.kind == ServiceKind.PERIODIC }
        ?.let { Odometer(it.km, it.date) }
        ?: Odometer(0, purchaseDate)

    fun nextService(today: LocalDate): PeriodicService = periodic(1, today)

    fun upcoming(today: LocalDate, count: Int = 8): List<PeriodicService> = (1..count).map { periodic(it, today) }

    private fun periodic(step: Int, today: LocalDate): PeriodicService {
        val number = periodicCount + step
        val dueDate = periodicBase.date.plusMonths(SERVICE_MONTHS.toLong() * step)
        val dueKm = periodicBase.km + SERVICE_KM * step
        val kmDate = dateForKm(dueKm, today)
        val limit = if (kmDate < dueDate) Limit.KM else Limit.DATE
        val effective = minOf(dueDate, kmDate)
        val position = ((number - 1) % PERIODIC_PATTERN.size) + 1
        val pattern = PERIODIC_PATTERN[position - 1]
        return PeriodicService(
            number = number,
            cyclePosition = position,
            dueDate = dueDate,
            dueKm = dueKm,
            effectiveDate = effective,
            limit = limit,
            expectedKm = if (limit == Limit.KM) dueKm else kmAtDate(effective),
            airFilter = pattern.airFilter,
            acFilter = pattern.acFilter,
            status = statusOf(today, dueDate, dueKm, effective),
            daysLeft = days(today, effective),
            kmLeft = dueKm - latest.km,
        )
    }

    fun intervalItems(today: LocalDate): List<IntervalDue> = INTERVAL_RULES.map { interval(it, today) }

    private fun interval(rule: IntervalRule, today: LocalDate): IntervalDue {
        val last = records.lastOrNull { rule.item in it.itemsDone }
        val lastDate = last?.date ?: purchaseDate
        val lastKm = last?.km ?: 0
        val dueKm = lastKm + rule.km
        val dueDate = rule.months?.let { lastDate.plusMonths(it.toLong()) }
        val kmDate = dateForKm(dueKm, today)
        val limit = if (dueDate != null && dueDate <= kmDate) Limit.DATE else Limit.KM
        val effective = if (dueDate != null) minOf(dueDate, kmDate) else kmDate
        val kmUsed = (latest.km - lastKm).coerceAtLeast(0) / rule.km.toDouble()
        val timeUsed = dueDate?.let { days(lastDate, today).toDouble() / max(1L, days(lastDate, it)) } ?: 0.0
        return IntervalDue(
            rule = rule,
            lastDate = lastDate,
            lastKm = lastKm,
            doneBefore = last != null,
            dueDate = dueDate,
            dueKm = dueKm,
            effectiveDate = effective,
            limit = limit,
            progress = max(kmUsed, timeUsed).coerceAtLeast(0.0),
            status = statusOf(today, dueDate, dueKm, effective),
            daysLeft = days(today, effective),
            kmLeft = dueKm - latest.km,
        )
    }

    private fun statusOf(today: LocalDate, dueDate: LocalDate?, dueKm: Int, effective: LocalDate): Status = when {
        (dueDate != null && today > dueDate) || latest.km >= dueKm -> Status.OVERDUE
        days(today, effective) <= DUE_SOON_DAYS || dueKm - latest.km <= DUE_SOON_KM -> Status.DUE_SOON
        else -> Status.OK
    }

    /** Work for a periodic visit: oil and filter, the two cycle filters, and any distance item due by then. */
    fun visitPlan(service: PeriodicService, today: LocalDate): List<VisitItem> {
        val base = listOf(
            VisitItem(ItemId.OIL_AND_FILTER, ItemId.OIL_AND_FILTER.label, Code.R.label, replace = true),
            VisitItem(ItemId.AIR_FILTER, ItemId.AIR_FILTER.label, service.airFilter.label, service.airFilter == Code.R),
            VisitItem(ItemId.AC_FILTER, ItemId.AC_FILTER.label, service.acFilter.label, service.acFilter == Code.R),
        )
        val due = intervalItems(today)
            .filter { dueByVisit(it, service) }
            .map { VisitItem(it.rule.item, it.rule.label, it.rule.verb, replace = true) }
        return base + due
    }

    private fun dueByVisit(item: IntervalDue, service: PeriodicService): Boolean =
        item.status != Status.OK ||
            item.dueKm <= service.expectedKm + PRETICK_KM ||
            (item.dueDate != null && item.dueDate <= service.effectiveDate)

    /** Items to pre-tick when logging a service on [date] at [km]. */
    fun suggestedItems(kind: ServiceKind, date: LocalDate, km: Int, today: LocalDate): Set<ItemId> = buildSet {
        if (kind == ServiceKind.PERIODIC) {
            val next = nextService(today)
            add(ItemId.OIL_AND_FILTER)
            if (next.airFilter == Code.R) add(ItemId.AIR_FILTER)
            if (next.acFilter == Code.R) add(ItemId.AC_FILTER)
        }
        if (kind == ServiceKind.PERIODIC || kind == ServiceKind.REPAIR) {
            intervalItems(today)
                .filter { it.dueKm - km <= PRETICK_KM || (it.dueDate != null && it.dueDate <= date) }
                .forEach { add(it.rule.item) }
        }
    }

    fun warranties(today: LocalDate): List<WarrantyState> =
        (MAIN_WARRANTIES + SPECIAL_PART_WARRANTIES).map {
            warranty(it.name, it.category, purchaseDate, 0, it.months, it.km, today, it.note)
        }

    /** Warranties for genuine parts fitted later, counted from the record that fitted them. */
    fun sparePartWarranties(today: LocalDate): List<WarrantyState> = records.flatMap { record ->
        record.fittedParts.map { part ->
            warranty(
                name = part.name.ifBlank { part.kind.label },
                category = WarrantyCategory.SPARE_PART,
                start = record.date,
                startKm = record.km,
                months = part.kind.months,
                km = part.kind.km,
                today = today,
                note = part.kind.label,
            )
        }
    }

    private fun warranty(
        name: String,
        category: WarrantyCategory,
        start: LocalDate,
        startKm: Int,
        months: Int,
        km: Int,
        today: LocalDate,
        note: String?,
    ): WarrantyState {
        val expiry = start.plusMonths(months.toLong())
        val limitKm = startKm + km
        val kmDate = dateForKm(limitKm, today)
        val limit = if (kmDate < expiry) Limit.KM else Limit.DATE
        val status = when {
            today >= expiry || latest.km >= limitKm -> WarrantyStatus.EXPIRED
            days(today, expiry) <= WARRANTY_SOON_DAYS || limitKm - latest.km <= WARRANTY_SOON_KM -> WarrantyStatus.ENDING_SOON
            else -> WarrantyStatus.ACTIVE
        }
        val timeUsed = days(start, today).toDouble() / max(1L, days(start, expiry))
        val kmUsed = (latest.km - startKm).coerceAtLeast(0) / km.toDouble()
        return WarrantyState(
            name = name,
            category = category,
            startDate = start,
            startKm = startKm,
            expiryDate = expiry,
            limitKm = limitKm,
            effectiveEnd = minOf(expiry, kmDate),
            limit = limit,
            progress = max(timeUsed, kmUsed).coerceIn(0.0, 1.0),
            status = status,
            daysLeft = days(today, expiry),
            kmLeft = limitKm - latest.km,
            note = note,
        )
    }

    companion object {
        const val DUE_SOON_DAYS = 14L
        const val DUE_SOON_KM = 500
        const val PRETICK_KM = 1_000
        const val WARRANTY_SOON_DAYS = 60L
        const val WARRANTY_SOON_KM = 2_000
        const val RATE_WINDOW_DAYS = 90L
        const val RATE_MIN_SPAN_DAYS = 30L

        fun days(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)
    }
}
