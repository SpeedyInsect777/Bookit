package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookitUtils;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DB_Util;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ApiStepDefs {

    String token;
    Response response;
    String emailGlobal;

    @Given("I logged Bookit api as a {string}")
    public void i_logged_bookit_api_as_a(String role) {
        token = BookitUtils.generateTokenByRole(role);
        System.out.println("token = " + token);

        Map<String, String> credentials = BookitUtils.returnCredentials(role);
        emailGlobal = credentials.get("email");


    }
    @When("I sent get request to {string} endpoint")
    public void i_sent_get_request_to_endpoint(String endpoint) {
         response = given().accept(ContentType.JSON)
                .header("Authorization", token)
                .when().get(Environment.BASE_URL + endpoint);

    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {

        System.out.println("response.statusCode() = " + response.statusCode());
        Assert.assertEquals(expectedStatusCode,response.statusCode());


    }
    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        System.out.println("response.contentType() = " + response.contentType());
        Assert.assertEquals(expectedContentType,response.contentType());

    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {

        System.out.println("response.path(\"role\") = " + response.path("role"));
        System.out.println("response.jsonPath().getString(\"role\") = " + response.jsonPath().getString("role"));

        Assert.assertEquals(expectedRole,response.path("role"));

    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match() {

       // GET DATA FROM API
        JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 11312,
            "firstName": "Lissie",
            "lastName": "Finnis",
            "role": "student-team-leader"
}
         */
        // lastname
        String actualLastName = jsonPath.getString("lastName");
        // firstname
        String actualFirstName = jsonPath.getString("firstName");
        // role
        String actualRole = jsonPath.getString("role");

        // GET DATA FROM DB

        String query="select firstname,lastname,role from users where email='"+emailGlobal+"'";

        DB_Util.runQuery(query);

        Map<String, String> dbMap = DB_Util.getRowMap(1);

        System.out.println(dbMap);


        String expectedFirstName = dbMap.get("firstname");
        String expectedLastName = dbMap.get("lastname");
        String expectedRole = dbMap.get("role");

        // ASSERTIONS

        Assert.assertEquals(expectedFirstName,actualFirstName);
        Assert.assertEquals(expectedLastName,actualLastName);
        Assert.assertEquals(expectedRole,actualRole);

    }

    @Then("UI,API and Database user information must be match")
    public void ui_api_and_database_user_information_must_be_match() {


        // GET DATA FROM API
        JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 11312,
            "firstName": "Lissie",
            "lastName": "Finnis",
            "role": "student-team-leader"
}
         */
        // lastname
        String actualLastName = jsonPath.getString("lastName");
        // firstname
        String actualFirstName = jsonPath.getString("firstName");
        // role
        String actualRole = jsonPath.getString("role");

        // GET DATA FROM DB

        String query="select firstname,lastname,role from users where email='"+emailGlobal+"'";

        DB_Util.runQuery(query);

        Map<String, String> dbMap = DB_Util.getRowMap(1);

        System.out.println(dbMap);


        String expectedFirstName = dbMap.get("firstname");
        String expectedLastName = dbMap.get("lastname");
        String expectedRole = dbMap.get("role");

        // API vs DB Assertion

        Assert.assertEquals(expectedFirstName,actualFirstName);
        Assert.assertEquals(expectedLastName,actualLastName);
        Assert.assertEquals(expectedRole,actualRole);


        // GET DATA FROM UI
        // There is no only firstname and lastname.It is stored as fullname

        SelfPage selfPage=new SelfPage();
        String actualNameFromUI = selfPage.name.getText();
        String actualRoleFromUI = selfPage.role.getText();


        // UI vs DB Assertion
        String expectedName=expectedFirstName+" "+expectedLastName;

        Assert.assertEquals(expectedName,actualNameFromUI);
        Assert.assertEquals(expectedRole,actualRoleFromUI);

        // UI vs API Assertion
        String actualNameFromAPI=actualFirstName+" "+actualLastName;
        Assert.assertEquals(actualNameFromAPI,actualNameFromUI);
        Assert.assertEquals(actualRole,actualRoleFromUI);

    }

    /**
     * ADD STUDENT
     */

    @When("I send POST request {string} endpoint with following information")
    public void i_send_post_request_endpoint_with_following_information(String endpoint, Map<String,String> userInfo) {

         response = given().accept(ContentType.JSON)
                .header("Authorization", token)
                .queryParams(userInfo).
                when().post(Environment.BASE_URL + endpoint).prettyPeek();

    }
    @Then("I delete previously added student")
    public void i_delete_previously_added_student() {

        int idToDelete = response.path("entryiId");
        System.out.println("entryiId = " + idToDelete);

        // ENDPOINT --> DELETE /api/students/{id} --> FROM DOCUMENT

        given().header("Authorization",token).
                pathParam("id",idToDelete).
        when().delete(Environment.BASE_URL+"/api/students/{id}").
                then().statusCode(204);

        /**
         * Can you explain this part where we need to get email?
         *  - you can use any email that you did not create as a student
         *
         *  We need to put any email in postman ?
         *   - No.Why?
         *     - We already created student in POSTMAN together.If the student already created can we use same email ?
         *          - NO
         *  Why we need curl and where we need to use it ?
         *    - This is just fro tool to share your request with your friends or peers.
         *
         *  Also we need to change only email and password part on scenario? ?
         *    - No.We did create one "And I delete previously added student" step to delete
         *      created students in saem feature by getting student id
         *
         *          int idToDelete = response.path("entryiId");
         */




    }

    /**
     *
     * ADD TEAM
     *
     */
    Map<String, String> newRecordMap;
    int newTeamID;

    @When("Users sends POST request to {string} with following info:")
    public void users_sends_post_request_to_with_following_info (String endpoint, Map<String, String> dataMap) {
        response =given().accept(ContentType.JSON)
                .and().queryParams(dataMap)
                .and().header("Authorization", token)
                .when().post(Environment.BASE_URL + endpoint);
        response.prettyPeek();

        //store into newRecordMap so that we can use for validation in next step
        newRecordMap = dataMap;



    }

    @Then("Database should persist same team info")
    public void database_should_persist_same_team_info() {
        response.prettyPeek();
        newTeamID = response.path("entryiId");

        System.out.println(" --------------> New Team is created --> ID is "+ newTeamID);

        String sql = "SELECT name,location,batch_number FROM team t inner join campus c on c.id = t.campus_id WHERE t.id =" + newTeamID;

        /**
         * SELECT name,location,batch_number FROM team t inner join campus c on c.id = t.campus_id
         *          WHERE t.id = 16664;
         */
        DB_Util.runQuery(sql);
        Map<String, String> dbNewTeamMap = DB_Util.getRowMap(1);

        System.out.println("sql = " + sql);
        System.out.println("dbNewTeamMap = " + dbNewTeamMap);

        assertThat(dbNewTeamMap.get("name"), equalTo(newRecordMap.get("team-name")));
        assertThat(dbNewTeamMap.get("location"), equalTo(newRecordMap.get("campus-location")));
        assertThat(dbNewTeamMap.get("batch_number"), equalTo(newRecordMap.get("batch-number")));
    }

    @Then("User deletes previously created team")
    public void user_deletes_previously_created_team() {
        given().accept(ContentType.JSON)
                .and().header("Authorization", token)
                .and().pathParam("id", newTeamID)
                .when().delete(Environment.BASE_URL + "/api/teams/{id}")
                .then().log().all();

        System.out.println("-------------->  New Team is deleted --> ID is "+ newTeamID);

    }

}
