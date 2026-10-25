package com.example.j5servicebook.domain

// Schedule and warranty data from the Jaecoo J5 SHS owner's manual (pp. 226–234)
// and warranty booklet (pp. 4–6). Nothing here is user-editable.

enum class ItemId(val label: String) {
    OIL_AND_FILTER("Engine oil and filter"),
    AIR_FILTER("Air filter"),
    AC_FILTER("A/C cabin filter"),
    TIRE_ROTATION("Tyre rotation"),
    THROTTLE_CLEAN("Throttle valve cleaning"),
    ALIGNMENT("Four-wheel alignment"),
    SPARK_PLUGS("Spark plugs"),
    BRAKE_FLUID("Brake fluid"),
    COOLANT("Coolant"),
    TRANSMISSION_FLUID("Transmission fluid"),
    FUEL_FILTER("Fuel filter"),
    CHARCOAL_CANISTER("Charcoal canister filter"),
    FUEL_FILLER_HOSE("Fuel filler hose"),
    FUEL_VAPOR_PIPE("Fuel vapour pipe"),
}

/** Manual codes. I = inspect, adjust, clean or replace if necessary; R = replace. */
enum class Code(val short: String, val label: String) {
    I("I", "Inspect"),
    IR("I/R", "Inspect or replace"),
    R("R", "Replace"),
}

data class CycleStep(val months: Int, val km: Int, val airFilter: Code, val acFilter: Code)

const val SERVICE_MONTHS = 3
const val SERVICE_KM = 5_000

/** The 10-visit cycle. After visit 10 it repeats from visit 1. */
val PERIODIC_PATTERN: List<CycleStep> = listOf(
    CycleStep(3, 5_000, Code.I, Code.I),
    CycleStep(6, 10_000, Code.IR, Code.IR),
    CycleStep(9, 15_000, Code.I, Code.R),
    CycleStep(12, 20_000, Code.R, Code.I),
    CycleStep(15, 25_000, Code.I, Code.IR),
    CycleStep(18, 30_000, Code.IR, Code.R),
    CycleStep(21, 35_000, Code.I, Code.I),
    CycleStep(24, 40_000, Code.R, Code.IR),
    CycleStep(27, 45_000, Code.I, Code.R),
    CycleStep(30, 50_000, Code.IR, Code.I),
)

data class IntervalRule(
    val item: ItemId,
    val km: Int,
    val months: Int?,
    val label: String,
    val detail: String,
    val verb: String,
    val noticeName: String,
)

val INTERVAL_RULES: List<IntervalRule> = listOf(
    IntervalRule(ItemId.TIRE_ROTATION, 10_000, null, "Tyre rotation", "Every 10,000 km · best at 5,000–7,000 km", "Rotate", "Tyre rotation"),
    IntervalRule(ItemId.THROTTLE_CLEAN, 15_000, null, "Throttle valve cleaning", "Every 15,000 km", "Clean", "Throttle valve cleaning"),
    IntervalRule(ItemId.ALIGNMENT, 20_000, null, "Four-wheel alignment", "Check every 20,000 km", "Check", "Wheel alignment check"),
    IntervalRule(ItemId.SPARK_PLUGS, 30_000, null, "Spark plugs", "Replace every 30,000 km · 3707AAG", "Replace", "Spark plug replacement"),
    IntervalRule(ItemId.BRAKE_FLUID, 40_000, 24, "Brake fluid", "Replace every 2 years or 40,000 km · DOT4", "Replace", "Brake fluid replacement"),
    IntervalRule(ItemId.COOLANT, 40_000, 24, "Coolant", "Replace every 2 years or 40,000 km · engine and battery loops", "Replace", "Coolant replacement"),
    IntervalRule(ItemId.TRANSMISSION_FLUID, 40_000, 24, "Transmission fluid", "Replace every 2 years or 40,000 km", "Replace", "Transmission fluid replacement"),
    IntervalRule(ItemId.FUEL_FILTER, 80_000, null, "Fuel filter", "Replace every 80,000 km · internal", "Replace", "Fuel filter replacement"),
    IntervalRule(ItemId.CHARCOAL_CANISTER, 60_000, 36, "Charcoal canister filter", "Replace every 3 years or 60,000 km · if fitted", "Replace", "Charcoal canister filter replacement"),
    IntervalRule(ItemId.FUEL_FILLER_HOSE, 200_000, 60, "Fuel filler hose", "Replace every 5 years or 200,000 km", "Replace", "Fuel filler hose replacement"),
    IntervalRule(ItemId.FUEL_VAPOR_PIPE, 500_000, 120, "Fuel vapour pipe", "Replace every 10 years or 500,000 km", "Replace", "Fuel vapour pipe replacement"),
)

val MAINTENANCE_FREE: List<String> = listOf("Timing chain", "12V battery")

val EVERY_VISIT_CHECKS: List<String> = listOf(
    "Instrument cluster",
    "Video and entertainment system",
    "Diagnostic tester scan",
    "Front and rear wiper blades",
    "Wiper system",
    "Cooling effect",
    "A/C system",
    "Coolant level",
    "Coolant freezing point",
    "Brake fluid level",
    "Brake fluid water content",
    "Engine oil level",
    "Transmission fluid level",
    "12V battery voltage",
    "Shock absorbers",
    "Drive shafts and boots",
    "Chassis bolt torque",
    "Steering gear",
    "Steering column",
    "Steering links, ball joints and boots",
    "Tyre appearance",
    "Tyre tread pattern",
    "Tyre inflation pressure",
    "Wheel bolt torque",
    "Brake pads",
    "Brake discs",
    "Engine oil pan and drain bolts",
    "Transmission housing",
    "Fluid lines (steering, brake, fuel) and wire harness",
    "Power battery surface (rust, deformation)",
    "Power battery fixing bolts",
    "Power battery vent valve and vent hole",
    "Power battery connector (dust, water, ageing)",
    "Low-voltage harness connectors",
    "High-voltage connectors and harness",
)

val FIRST_SERVICE_CHECKLIST: List<String> = listOf(
    "Lubrication, cooling and fuel leaks",
    "Replace engine oil and filter",
    "Transmission fluid level and colour",
    "Coolant level and concentration",
    "Transmission leaks",
    "Steering tie-rod boots",
    "Drive shaft boots",
    "Brake fluid leaks and pipes",
    "Brake wheel cylinders",
    "Tyre pressures",
    "Alternator and A/C belt tension",
    "Hood hinge and latch",
    "A/C leaks",
    "Steering universal joint boot",
    "Smooth gear shifting",
    "Chassis-to-body bolts",
    "Seat belts",
    "Engine idle, injection and emissions",
    "Road test",
    "Power battery surface",
    "Power battery fixing bolts",
    "Power battery vent valve",
    "Power battery connector",
    "Low-voltage harness connectors",
    "High-voltage connectors and harness",
)

const val SEVERE_USE_NOTE =
    "High humidity, mountains, extreme heat or cold, rough roads, frequent short trips, " +
        "heavy traffic above 32 °C or commercial use mean items should be replaced more often. " +
        "Intervals shown here are the standard ones."

data class FluidSpec(val item: String, val spec: String, val capacity: String?, val note: String? = null)

val FLUID_SPECS: List<FluidSpec> = listOf(
    FluidSpec("Engine oil", "C3 0W-30", "4.5 ± 0.2 L with filter"),
    FluidSpec("Transmission fluid", "TITAN EG DHT 5105", "4.1 ± 0.1 L", "The schedule page names it 130HHB. Use what the dealer recommends."),
    FluidSpec("Engine coolant (high temp)", "OAT", "7.1 ± 0.5 L"),
    FluidSpec("Power battery coolant (low temp)", "OAT", "3.5 ± 0.5 L"),
    FluidSpec("Brake fluid", "DOT4", "Between MIN and MAX"),
    FluidSpec("Spark plug", "3707AAG", null),
)

enum class WarrantyCategory { MAIN, SPECIAL, SPARE_PART }

data class WarrantyRule(val name: String, val months: Int, val km: Int, val category: WarrantyCategory, val note: String? = null)

val MAIN_WARRANTIES: List<WarrantyRule> = listOf(
    WarrantyRule("Whole vehicle", 72, 150_000, WarrantyCategory.MAIN),
    WarrantyRule("Power battery cells", 96, 160_000, WarrantyCategory.MAIN),
    WarrantyRule("Paint and surface corrosion", 24, 50_000, WarrantyCategory.MAIN, "Not perforation"),
)

val SPECIAL_PART_WARRANTIES: List<WarrantyRule> = listOf(
    WarrantyRule("Tyres", 6, 10_000, WarrantyCategory.SPECIAL),
    WarrantyRule("12V battery", 12, 30_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Remote key battery", 12, 30_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Exhaust system", 24, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Timing belt", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Fuel injector", 48, 100_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Fuel pump", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Oil pump", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Water pump", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Brake disc", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Brake caliper", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("CD/DVD", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Starter", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Alternator", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Hub bearing", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Window glass regulator", 36, 60_000, WarrantyCategory.SPECIAL),
    WarrantyRule("A/C compressor", 48, 100_000, WarrantyCategory.SPECIAL),
    WarrantyRule("Shock absorber", 36, 60_000, WarrantyCategory.SPECIAL),
)

/** Genuine parts bought and fitted at an authorised dealer, counted from the fitting date. */
enum class SparePartKind(val label: String, val months: Int, val km: Int) {
    STANDARD("Other genuine part", 12, 20_000),
    GLASS("Glass or mirror", 3, 5_000),
    WIPER_BLADES("Wiper blades", 3, 5_000),
    CLUTCH_PLATE("Clutch plate", 3, 5_000),
    BRAKE_DISC_DRUM("Brake disc or drum", 3, 5_000),
    BRAKE_PADS_SHOES("Brake pads or shoes", 3, 5_000),
}

const val WARRANTY_KM_NOTE =
    "Km are read from the instrument cluster. If the cluster is damaged, 3,000 km per month is counted."

val WARRANTY_CONDITIONS: List<String> = listOf(
    "Use only genuine parts and fluids.",
    "Use only brands and specs recommended by the authorised service station.",
    "Have scheduled services done at an authorised OMODA & JAECOO dealer, or claim rights may be affected.",
)

val WARRANTY_EXCLUSIONS: List<String> = listOf(
    "Bulbs", "Fuses", "Lubricating oil", "Air filter", "Oil filter",
    "Fuel filter", "A/C filter", "Spark plugs", "Coolant",
)
