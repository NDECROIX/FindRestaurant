package com.decroix.nicolas.go4lunch;

import com.decroix.nicolas.go4lunch.utils.UsefulFunctions;
import com.google.android.libraries.places.api.model.OpeningHours;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests functions that have objects of the api google map/place as parameters
 */
public class UsefulFunctionsUnitTest {

    /**
     * Retrieves the restaurant's opening hours
     */
    @Test
    public void getOpeningHoursTest() {
        String day = "Monday: 12:00 - 2:30 PM, 7:30 - 10:30 PM";
        String expected = " 12:00 - 2:30 PM, 7:30 - 10:30 PM";
        List<String> sevenDays = Arrays.asList(day, day, day, day, day, day, day);
        OpeningHours openingHours = mock(OpeningHours.class);
        when(openingHours.getWeekdayText()).thenReturn(sevenDays);
        String result = UsefulFunctions.getOpeningHours(null, openingHours);
        assertEquals(expected, result);
    }

    /**
     * Convert a rating from 5 to 3
     */
    @Test
    public void parseRatingTest() {
        assertEquals(2, UsefulFunctions.parseRating(4d), 0.0);
    }

    /**
     * Convert the date to time
     */
    @Test
    public void convertDateToHourTest(){
        Date date = new Date();
        date.setTime(Calendar.getInstance().getTimeInMillis());
        String result = UsefulFunctions.convertDateToHour(date);
        String expected = new SimpleDateFormat(" E HH:mm", Locale.FRANCE).format(date);
        assertEquals(expected, result);
    }
}