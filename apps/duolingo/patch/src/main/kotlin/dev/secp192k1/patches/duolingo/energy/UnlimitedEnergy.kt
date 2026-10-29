package dev.secp192k1.patches.duolingo.energy

import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.DeveloperSettings
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

private const val INTEGER = "Ljava/lang/Integer;"
private const val USER = "Lcom/duolingo/data/user/User;"
private const val COURSE_STATUS = "Lcom/duolingo/data/path/common/CourseStatus;"

private const val ENERGY_VALUE = 1337
private const val MAX_ENERGY_VALUE = 1337

// EnergyConfig is a data class, so its generated toString keeps the original field names and anchors the class
val energyConfig = klass("energyConfig") {
    strings("EnergyConfig(energy=")
}

// The public constructor backs normal creation and the copies Duolingo makes as energy changes
val energyConfigCreate = energyConfig.method("<init>") {
    params(Type.Int, Type.Int, Type.Long, Type.Long, Type.Long, Type.Int, Type.Long, INTEGER)
}

// kotlinx.serialization builds an EnergyConfig from the server's JSON through this synthetic constructor
// Its last parameter is the boolean field, not a SerializationConstructorMarker, so the signature ends in Type.Boolean
val energyConfigDeserialize = energyConfig.method("<init>") {
    params(Type.Int, Type.Int, Type.Int, Type.Long, Type.Long, Type.Long, Type.Int, Type.Long, INTEGER, Type.Boolean)
}

// energy is the first field the constructor stores, so it is the first int field the method writes
val energyWrite = energyConfigCreate.point("energyWrite") {
    field { type(Type.Int) }
}

val energy = energyWrite.field("energy")

// maxEnergy is the int field the constructor stores right after energy
val maxEnergy = energyWrite.next { field { type(Type.Int) } }.field("maxEnergy")

// For accounts without a subscription, this check says whether an active Unlimited Energy Booster or a beta course lifts the energy limit
// The energy counter, the session screen and every out of energy gate ask it, and it is the only boolean method that reads CourseStatus.BETA
val hasFreeUnlimitedEnergy = method("hasFreeUnlimitedEnergy") {
    accesses { owner(COURSE_STATUS); name("BETA") }
    hasParam(USER)
    returns(Type.Boolean)
}

@Suppress("unused")
val unlimitedEnergy = patch("Unlimited energy") {
    description("Gives you unlimited energy, so lessons and mistakes never use it up.")
    // Pinned because a wrong match would overwrite two fields without any error
    compatibleWith(DUOLINGO_PINNED)
    // Applied by default so its switch always shows in Settings, where it can be turned off
    settings(duolingoSettings, section(DuolingoStrings.sectionDeveloper, DeveloperSettings.unlimitedEnergy))

    execute {
        gate(DeveloperSettings.unlimitedEnergy) {
            // Turned on, this makes the app treat energy as unlimited and show the infinity icon in place of the energy count
            // It stays off so the counters show the values written below
            // hasFreeUnlimitedEnergy.alwaysReturn(true)
            // Overwrite both values after each build, so every config the client holds reports them
            energyConfigCreate.after {
                thisObject.set(energy, int(ENERGY_VALUE))
                thisObject.set(maxEnergy, int(MAX_ENERGY_VALUE))
            }
            energyConfigDeserialize.after {
                thisObject.set(energy, int(ENERGY_VALUE))
                thisObject.set(maxEnergy, int(MAX_ENERGY_VALUE))
            }
        }
    }
}
