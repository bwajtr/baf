package com.wajtr.baf.ui.components

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.ibm.icu.text.Collator
import com.ibm.icu.text.LocaleDisplayNames
import com.ibm.icu.util.Region
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.server.VaadinSession
import java.util.*

/**
 *
 * @author Bretislav Wajtr
 */

typealias CountryCode = String

class CountrySelectComboBox : ComboBox<CountryCode> {

    constructor() : super()
    constructor(caption: String?) : super(caption)
    constructor(caption: String?, options: MutableCollection<CountryCode>?) : super(caption, options)

    init {
        isAllowCustomValue = false
        isClearButtonVisible = true

        val availableCountries = Region.getAvailable(Region.RegionType.TERRITORY)
        val localeDisplayNames = LocaleDisplayNames.getInstance(VaadinSession.getCurrent().locale)

        val countryCaptions = availableCountries
            .associate {
                val regionCode = it.toString()
                val regionName = localeDisplayNames.regionDisplayName(regionCode) + " ($regionCode)"
                Pair(regionCode, regionName)
            }

        val sortedCountryCodes = countryCaptions.entries
            .sortedWith(ByCountryCaptionComparator())
            .map { it.key }

        setItems(sortedCountryCodes)

        setItemLabelGenerator { countryCode ->
            countryCaptions[countryCode]
        }
    }

    /**
     * This comparator uses ICU4J locale collator to properly sort the countries in the combobox according to the users language usual sorting
     */
    private class ByCountryCaptionComparator : Comparator<Map.Entry<String, String>> {
        private val icu4jCollator = Collator.getInstance(VaadinSession.getCurrent().locale)

        override fun compare(o1: Map.Entry<String, String>, o2: Map.Entry<String, String>): Int {
            return icu4jCollator.compare(o1.value, o2.value)
        }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).countrySelectCombo(
    caption: String? = null,
    block: (@VaadinDsl CountrySelectComboBox).() -> Unit = {}
) = init(CountrySelectComboBox(caption), block)