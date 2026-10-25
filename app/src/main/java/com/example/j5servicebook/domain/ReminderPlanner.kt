package com.example.j5servicebook.domain

import java.time.LocalDate

/**
 * A notification to post. [key] identifies the stage so it is only sent once;
 * [tag] identifies the subject so a later stage replaces an earlier one in the shade.
 */
data class Notice(
    val key: String,
    val tag: String,
    val title: String,
    val text: String,
    val logAction: Boolean = false,
)

object ReminderPlanner {
    const val OVERDUE_REPEAT_DAYS = 3L
    const val ODOMETER_STALE_DAYS = 30L
    const val WARRANTY_NOTICE_DAYS = 30L

    fun plan(engine: ScheduleEngine, today: LocalDate, leadDays: Int, sent: Set<String>): List<Notice> = buildList {
        serviceNotice(engine.nextService(today), today, leadDays)?.let(::add)

        engine.intervalItems(today)
            .filter { it.status != Status.OK }
            .forEach { item ->
                val overdue = item.status == Status.OVERDUE
                add(
                    Notice(
                        key = "item-${item.rule.item}-${item.lastDate}-${item.status}",
                        tag = "item-${item.rule.item}",
                        title = if (overdue) "${item.rule.noticeName} overdue" else "${item.rule.noticeName} due ${Fmt.date(item.effectiveDate)}",
                        text = "${item.rule.detail}. Due at ${Fmt.km(item.dueKm)}.",
                    ),
                )
            }

        val stale = ScheduleEngine.days(engine.latest.date, today)
        if (stale >= ODOMETER_STALE_DAYS) {
            add(
                Notice(
                    key = "odometer-${engine.latest.date}-${stale / ODOMETER_STALE_DAYS}",
                    tag = "odometer",
                    title = "Update your odometer",
                    text = "So due dates stay accurate. Last reading ${Fmt.km(engine.latest.km)} on ${Fmt.date(engine.latest.date)}.",
                ),
            )
        }

        (engine.warranties(today) + engine.sparePartWarranties(today))
            .filter { it.status != WarrantyStatus.EXPIRED && it.daysLeft in 0..WARRANTY_NOTICE_DAYS }
            .forEach {
                add(
                    Notice(
                        key = "warranty-${it.name}-${it.startDate}-${it.expiryDate}",
                        tag = "warranty-${it.name}-${it.startDate}",
                        title = "${it.name} warranty ends ${Fmt.date(it.expiryDate)}",
                        text = "Or at ${Fmt.km(it.limitKm)}, whichever comes first.",
                    ),
                )
            }
    }.filter { it.key !in sent }

    private fun serviceNotice(s: PeriodicService, today: LocalDate, leadDays: Int): Notice? {
        val work = workSummary(s)
        val tag = "service"
        return when {
            s.status == Status.OVERDUE -> Notice(
                key = "service-${s.number}-overdue-${today.toEpochDay() / OVERDUE_REPEAT_DAYS}",
                tag = tag,
                title = "Service ${s.number} is overdue",
                text = "It was due ${Fmt.date(s.effectiveDate)} or ${Fmt.km(s.dueKm)}. $work, book the dealer.",
                logAction = true,
            )
            s.daysLeft <= 0 || s.kmLeft <= 0 -> Notice(
                key = "service-${s.number}-due",
                tag = tag,
                title = "Service ${s.number} is due today",
                text = "$work, book the dealer.",
                logAction = true,
            )
            s.daysLeft <= leadDays -> Notice(
                key = "service-${s.number}-lead",
                tag = tag,
                title = "Service ${s.number} due ${Fmt.shortDate(s.effectiveDate)}",
                text = "$work, book the dealer.",
                logAction = true,
            )
            else -> null
        }
    }

    private fun workSummary(s: PeriodicService): String {
        val extra = buildList {
            if (s.airFilter == Code.R) add("air filter")
            if (s.acFilter == Code.R) add("A/C filter")
        }
        return if (extra.isEmpty()) "Oil and filter change" else "Oil and filter change plus ${extra.joinToString(" and ")}"
    }
}
