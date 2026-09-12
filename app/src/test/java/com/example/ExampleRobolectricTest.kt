package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ApartmentEntity
import com.example.data.model.FilterState
import com.example.data.model.FurnitureFilter
import com.example.data.model.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Rentch app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rentch", appName)
    }

    @Test
    fun `user profile registration and questionnaire model integrity`() {
        val profile = UserProfileEntity(
            isRegistered = true,
            name = "Алексей",
            phone = "+995 599 123 456",
            rentalPeriod = "От месяца до года",
            tenantsCount = 2,
            hasPets = true,
            petType = "Кошка",
            preferredDistrict = "Ваке"
        )
        assertTrue(profile.isRegistered)
        assertEquals("Ваке", profile.preferredDistrict)
        assertEquals(2, profile.tenantsCount)
        assertTrue(profile.hasPets)
    }

    @Test
    fun `filter state budget and furniture matching logic`() {
        val filter = FilterState(
            minBudget = 500,
            maxBudget = 1000,
            district = "Ваке",
            furnitureRequirement = FurnitureFilter.WITH_FURNITURE,
            petFriendlyOnly = true
        )

        val matchingApt = ApartmentEntity(
            id = 1,
            title = "Уютная квартира",
            district = "Ваке",
            address = "Проспект Чавчавадзе, 24",
            priceUsd = 950,
            rooms = 2,
            areaSqM = 68,
            hasFurniture = true,
            petsAllowed = true,
            minPeriod = "От месяца до года",
            maxTenants = 2,
            floor = "7/12",
            imageDrawableName = "img_apt_vake",
            description = "Красивая квартира в Ваке",
            features = "Кондиционер, Балкон",
            landlordName = "Георгий",
            landlordPhone = "+995 555 11 22 33",
            lat = 41.7100,
            lng = 44.7500
        )

        val priceFits = matchingApt.priceUsd in filter.minBudget..filter.maxBudget
        val districtFits = filter.district == "Все районы" || matchingApt.district.equals(filter.district, ignoreCase = true)
        val furnitureFits = when (filter.furnitureRequirement) {
            FurnitureFilter.ANY -> true
            FurnitureFilter.WITH_FURNITURE -> matchingApt.hasFurniture
            FurnitureFilter.WITHOUT_FURNITURE -> !matchingApt.hasFurniture
        }
        val petsFit = !filter.petFriendlyOnly || matchingApt.petsAllowed

        assertTrue(priceFits)
        assertTrue(districtFits)
        assertTrue(furnitureFits)
        assertTrue(petsFit)
    }
}
