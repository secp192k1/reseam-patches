package dev.secp192k1.patches.anydesk.settings

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.field
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.settings.openSettings
import app.reseam.patch.settings.settingsHost
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.brand.SETTINGS_TITLE
import dev.secp192k1.patches.brand.publisherLinks
import dev.secp192k1.patches.brand.reseamBrand

private const val ARRAY_ADAPTER = "android.widget.ArrayAdapter"
private const val LIST_VIEW = "Landroid/widget/ListView;"
private const val SETTINGS_CATEGORY_FRAGMENT = "com.anydesk.anydeskandroid.gui.fragment.SettingsCategoryFragment"

// fragment_settings names this fragment in its layout, so R8 keeps the class name
val settingsCategoryFragment = klass(SETTINGS_CATEGORY_FRAGMENT)

val settingsCategoryList = method("settingsCategoryList") {
    inClass(settingsCategoryFragment)
    strings("s_application")
}

val categoryAdapter = settingsCategoryList.point("categoryAdapter") {
    invokeVirtual { owner(ARRAY_ADAPTER); name("clear") }
}.writer(0).field()

val applicationHeader = settingsCategoryList.point("applicationHeader") {
    invokeDirect { name("<init>"); params(Type.String, Type.String) }
    argument(1) { string("s_application") }
}

val sectionHeader = applicationHeader.callee()

// Every entry takes its icon, destination, fragment class, key and title
val categoryEntry = applicationHeader.next {
    invokeDirect { name("<init>"); params(Type.Int, Type.Int, "Ljava/lang/Class;", Type.String, Type.String) }
}.callee()

val showSettingsCategory = method("showSettingsCategory") {
    inClass(settingsCategoryFragment)
    params(LIST_VIEW, Type.Int, Type.Boolean)
}

val selectedCategory = showSettingsCategory.field("selectedCategory") {
    owner(SETTINGS_CATEGORY_FRAGMENT)
    type(Type.Int)
}

val settingsCategoryTap = method("settingsCategoryTap") {
    inClass(settingsCategoryFragment)
    params(LIST_VIEW, Type.Int)
    returns(Type.Void)
}

val anyDeskSettings = settingsHost("anydesk") {
    compatibleWith(ANYDESK)
    brand(reseamBrand)
    about(publisherLinks)

    install {
        val wrenchIconId = resources.id("drawable", "ic_nav_settings")?.toInt() ?: error("The app has no drawable/ic_nav_settings")

        settingsCategoryList.after {
            val adapter = thisObject.field(categoryAdapter)
            whenNotNull(adapter) {
                val header = newInstance(sectionHeader.owner, sectionHeader.proto, string("s_reseam"), string(reseamBrand.name))
                adapter.callVirtual(ARRAY_ADAPTER, "add", "(Ljava/lang/Object;)V", header)
                val entry = newInstance(
                    categoryEntry.owner,
                    categoryEntry.proto,
                    int(wrenchIconId),
                    int(0),
                    // No category fragment belongs to the entry, since a tap on it opens the Reseam Settings screen
                    nullObject,
                    string("s_reseam_settings"),
                    string(SETTINGS_TITLE),
                )
                adapter.callVirtual(ARRAY_ADAPTER, "add", "(Ljava/lang/Object;)V", entry)
            }
        }

        // The entry is always the last row, and the tap already checked it, so the open category is checked again
        settingsCategoryTap.before {
            val list = param(0)
            val lastRow = list.callVirtual("android.widget.AdapterView", "getCount", "()I").minus(int(1))
            whenEqual(param(1), lastRow) {
                list.callVirtual("android.widget.AbsListView", "setItemChecked", "(IZ)V", thisObject.field(selectedCategory), bool(true))
                openSettings()
                returnVoid()
            }
        }
    }
}
