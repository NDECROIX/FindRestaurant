package com.decroix.nicolas.go4lunch;

import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.google.android.libraries.places.api.model.Place;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestaurantUnitTest {

    /**
     * Convert a place to restaurant
     */
    @Test
    public void testConvertPlaceToRestaurant(){
        Place place = mock(Place.class);
        when(place.getId()).thenReturn("1576011764");
        when(place.getName()).thenReturn("Name");
        when(place.getAddress()).thenReturn("my address");

        Restaurant restaurant = new Restaurant(place);
        assertEquals(restaurant.getPlaceID(),"1576011764");
        assertEquals(restaurant.getName(),"Name");
        assertEquals(restaurant.getAddress(),"my address");
        assertEquals(restaurant.getUsers(),new ArrayList<>());
    }
}
