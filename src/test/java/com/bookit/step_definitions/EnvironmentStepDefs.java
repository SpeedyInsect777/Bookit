package com.bookit.step_definitions;

import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.Given;
import org.junit.Test;

public class EnvironmentStepDefs {

    @Given("I get related environment information")
    public void i_get_related_environment_information() {

        System.out.println("Environment.URL = " + Environment.URL);
        System.out.println("Environment.BASE_URL = " + Environment.BASE_URL);
        System.out.println("Environment.DB_URL = " + Environment.DB_URL);
//
    }

    @Given("I get related field data from maven")
    public void i_get_related_field_data_from_maven() {
        String teacher_email =
           System.getProperty("teacher_email") != null ?teacher_email = System.getProperty("teacher_email")
                                                 : Environment.TEACHER_EMAIL;

        System.out.println(teacher_email);


        // mvn test -Denvironment=qa3 -Dcucumber.filter.tags=@property -Dteacher_email=mike@smith.com -DbrowserEx=InternetExplorer

        String browserEx=
                System.getProperty("browserEx") != null ? browserEx = System.getProperty("browserEx")
                                                 : ConfigurationReader.getProperty("browserEx");

        System.out.println("browserEx = " + browserEx);

    }
}
