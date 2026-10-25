package com.example.j5servicebook.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ScheduleEngineTest {
    private val purchase = LocalDate.of(2026, 8, 20)
    private val today = LocalDate.of(2026, 10, 25)
    private val inspection = ServiceRecord(id = 1, date = LocalDate.of(2026, 9, 21), km = 1_000, kind = ServiceKind.INSPECTION)
    private val reading = Reading(date = today, km = 2_000)

    private fun engine(records: List<ServiceRecord> = listOf(inspection), readings: List<Reading> = listOf(reading)) =
        ScheduleEngine(purchase, readings, records)

    @Nested
    inner class OwnerData {
        private val e = engine()

        @Test
        fun `driving rate is about 30 point 3 km per day`() {
            assertEquals(30.3, e.kmPerDay, 0.05)
        }

        @Test
        fun `inspection does not count, next is service 1 due 20 Nov 2026 by date`() {
            val next = e.nextService(today)
            assertEquals(1, next.number)
            assertEquals(LocalDate.of(2026, 11, 20), next.effectiveDate)
            assertEquals(Limit.DATE, next.limit)
            assertTrue(next.expectedKm in 2_700..2_900, "expected ~2,800 km, was ${next.expectedKm}")
            assertEquals(Code.I, next.airFilter)
            assertEquals(Code.I, next.acFilter)
        }

        @Test
        fun `service 2 is due 20 Feb 2027`() {
            val s2 = e.upcoming(today)[1]
            assertEquals(2, s2.number)
            assertEquals(LocalDate.of(2027, 2, 20), s2.effectiveDate)
        }

        @Test
        fun `A-C filter is replaced at service 3 on 20 May 2027`() {
            val s3 = e.upcoming(today)[2]
            assertEquals(LocalDate.of(2027, 5, 20), s3.effectiveDate)
            assertEquals(Code.R, s3.acFilter)
        }

        @Test
        fun `two-year fluids are due 20 Aug 2028`() {
            val byItem = e.intervalItems(today).associateBy { it.rule.item }
            listOf(ItemId.BRAKE_FLUID, ItemId.COOLANT, ItemId.TRANSMISSION_FLUID).forEach {
                assertEquals(LocalDate.of(2028, 8, 20), byItem.getValue(it).effectiveDate, it.name)
                assertEquals(Limit.DATE, byItem.getValue(it).limit, it.name)
            }
        }

        @Test
        fun `warranty expiry dates`() {
            val w = e.warranties(today).associateBy { it.name }
            assertEquals(LocalDate.of(2027, 2, 20), w.getValue("Tyres").expiryDate)
            assertEquals(LocalDate.of(2032, 8, 20), w.getValue("Whole vehicle").expiryDate)
            assertEquals(LocalDate.of(2034, 8, 20), w.getValue("Power battery cells").expiryDate)
            assertEquals(WarrantyStatus.ACTIVE, w.getValue("Whole vehicle").status)
        }

        @Test
        fun `latest odometer is the highest km across readings and records`() {
            assertEquals(Odometer(2_000, today), e.latest)
        }
    }

    @Test
    fun `logging a periodic service moves the next due date forward 3 months`() {
        val s1 = ServiceRecord(id = 2, date = LocalDate.of(2026, 11, 21), km = 2_850, kind = ServiceKind.PERIODIC, itemsDone = setOf(ItemId.OIL_AND_FILTER))
        val e = engine(listOf(inspection, s1))
        val next = e.nextService(LocalDate.of(2026, 11, 21))
        assertEquals(2, next.number)
        assertEquals(LocalDate.of(2027, 2, 21), next.dueDate)
        assertEquals(2_850 + 5_000, next.dueKm)
    }

    @Test
    fun `cycle repeats after service 10`() {
        val records = (1..10).map {
            ServiceRecord(id = it.toLong(), date = purchase.plusMonths(3L * it), km = 1_000 * it, kind = ServiceKind.PERIODIC)
        }
        val e = ScheduleEngine(purchase, emptyList(), records)
        val next = e.nextService(purchase.plusMonths(30))
        assertEquals(11, next.number)
        assertEquals(1, next.cyclePosition)
    }

    @Test
    fun `km limit wins for a heavy driver`() {
        val e = ScheduleEngine(purchase, listOf(Reading(LocalDate.of(2026, 9, 20), 4_000)), emptyList())
        val next = e.nextService(LocalDate.of(2026, 9, 20))
        assertEquals(Limit.KM, next.limit)
        assertEquals(5_000, next.expectedKm)
        assertEquals(LocalDate.of(2026, 9, 28), next.effectiveDate)
        assertEquals(Status.DUE_SOON, next.status)
    }

    @Test
    fun `status overdue by date, due soon within 14 days`() {
        val e = engine()
        assertEquals(Status.DUE_SOON, e.nextService(LocalDate.of(2026, 11, 10)).status)
        assertEquals(Status.OVERDUE, e.nextService(LocalDate.of(2026, 11, 21)).status)
        assertEquals(Status.OK, e.nextService(today).status)
    }

    @Test
    fun `month arithmetic clamps to the last day`() {
        val e = ScheduleEngine(LocalDate.of(2027, 11, 30), emptyList(), emptyList())
        assertEquals(LocalDate.of(2028, 2, 29), e.nextService(LocalDate.of(2027, 12, 1)).dueDate)
    }

    @Test
    fun `km projection uses the driving rate`() {
        val e = engine()
        assertEquals(today, e.dateForKm(1_500, today))
        assertEquals(today.plusDays(99), e.dateForKm(5_000, today))
        assertEquals(2_000 + 303, e.kmAtDate(today.plusDays(10)))
    }

    @Test
    fun `interval item counts from the last record that did it`() {
        val rotation = ServiceRecord(id = 3, date = LocalDate.of(2026, 10, 1), km = 1_500, kind = ServiceKind.REPAIR, itemsDone = setOf(ItemId.TIRE_ROTATION))
        val item = engine(listOf(inspection, rotation)).intervalItems(today).first { it.rule.item == ItemId.TIRE_ROTATION }
        assertTrue(item.doneBefore)
        assertEquals(11_500, item.dueKm)
    }

    @Test
    fun `periodic log pre-ticks oil and any filter coded R`() {
        val records = listOf(
            ServiceRecord(id = 1, date = LocalDate.of(2026, 11, 20), km = 2_800, kind = ServiceKind.PERIODIC),
            ServiceRecord(id = 2, date = LocalDate.of(2027, 2, 20), km = 5_500, kind = ServiceKind.PERIODIC),
        )
        val day = LocalDate.of(2027, 5, 20)
        val e = ScheduleEngine(purchase, emptyList(), records)
        val items = e.suggestedItems(ServiceKind.PERIODIC, day, 8_200, day)
        assertEquals(setOf(ItemId.OIL_AND_FILTER, ItemId.AC_FILTER), items)
    }

    @Test
    fun `spare part warranty starts at the fitting record`() {
        val repair = ServiceRecord(
            id = 4, date = LocalDate.of(2026, 10, 1), km = 1_600, kind = ServiceKind.REPAIR,
            fittedParts = listOf(FittedPart("Front pads", SparePartKind.BRAKE_PADS_SHOES), FittedPart("Headlamp", SparePartKind.STANDARD)),
        )
        val w = engine(listOf(inspection, repair)).sparePartWarranties(today)
        assertEquals(LocalDate.of(2027, 1, 1), w[0].expiryDate)
        assertEquals(6_600, w[0].limitKm)
        assertEquals(LocalDate.of(2027, 10, 1), w[1].expiryDate)
        assertEquals(21_600, w[1].limitKm)
    }
}

class ReminderPlannerTest {
    private val purchase = LocalDate.of(2026, 8, 20)
    private val engine = ScheduleEngine(purchase, listOf(Reading(LocalDate.of(2026, 10, 25), 2_000)), emptyList())

    @Test
    fun `service lead notice fires inside the lead window only once`() {
        val day = LocalDate.of(2026, 11, 13)
        val notices = ReminderPlanner.plan(engine, day, leadDays = 7, sent = emptySet())
        val service = notices.single { it.tag == "service" }
        assertEquals("Service 1 due 20 Nov", service.title)
        assertTrue(service.logAction)
        val again = ReminderPlanner.plan(engine, day, leadDays = 7, sent = notices.map { it.key }.toSet())
        assertTrue(again.none { it.tag == "service" })
    }

    @Test
    fun `nothing for the service outside the lead window`() {
        val notices = ReminderPlanner.plan(engine, LocalDate.of(2026, 11, 1), leadDays = 7, sent = emptySet())
        assertTrue(notices.none { it.tag == "service" })
    }

    @Test
    fun `due today then overdue every 3 days`() {
        assertEquals(
            "Service 1 is due today",
            ReminderPlanner.plan(engine, LocalDate.of(2026, 11, 20), 7, emptySet()).single { it.tag == "service" }.title,
        )
        val keys = (21..30).map { d ->
            ReminderPlanner.plan(engine, LocalDate.of(2026, 11, d), 7, emptySet()).single { it.tag == "service" }.key
        }.toSet()
        assertTrue(keys.size in 3..4, "expected a new overdue key every 3 days, got $keys")
    }

    @Test
    fun `stale odometer and ending tyre warranty`() {
        val notices = ReminderPlanner.plan(engine, LocalDate.of(2027, 1, 25), 7, emptySet())
        assertTrue(notices.any { it.tag == "odometer" })
        assertTrue(notices.any { it.title == "Tyres warranty ends 20 Feb 2027" })
    }
}

class BackupCodecTest {
    @Test
    fun `round trip keeps everything`() {
        val backup = Backup(
            exportedAt = "2026-10-25T09:00",
            profile = CarProfile("Jaecoo J5 SHS", LocalDate.of(2026, 8, 20), 1L),
            readings = listOf(Reading(LocalDate.of(2026, 10, 25), 2_000, ReadingSource.MANUAL, 1)),
            records = listOf(
                ServiceRecord(
                    id = 1, date = LocalDate.of(2026, 9, 21), km = 1_000, kind = ServiceKind.INSPECTION,
                    itemsDone = setOf(ItemId.TIRE_ROTATION), workshop = "Dealer", cost = 0,
                    fittedParts = listOf(FittedPart("Wipers", SparePartKind.WIPER_BLADES)),
                ),
            ),
            leadDays = 7,
        )
        assertEquals(backup, BackupCodec.decode(BackupCodec.encode(backup)))
    }
}
