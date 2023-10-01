
package com.example.geodo;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.parse.ParseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class SignInTests {

    private static final String BASIC_SAMPLE_PACKAGE
            = "com.example.geodo";
    private static final int LAUNCH_TIMEOUT = 5000;
    private UiDevice mDevice;
    TaskSharedPreferences preferences;

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the blueprint app
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);// Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void checkPreconditions() {
        assertThat(mDevice, notNullValue());
    }

    // Log in Tests
    @Test
    public void LogIn_ValidUserNameAndPassword_LogInSuccess() {

        String NewUsername = "james@james.com";
        String NewPassword = "Shiels@123";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewLogOut")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Welcome, " + NewUsername + "! Click here to Log Out!")));
    }

    @Test
    public void LogIn_InvalidPassword_LogInFailure() {

        String NewUsername = "james@james.com";
        String NewPassword = "test";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewInvalid")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Invalid Username or Password")));
    }

    @Test
    public void LogIn_InvalidUsername_LogInFailure() {

        String NewUsername = "james";
        String NewPassword = "Test@123";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewInvalid")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Invalid Username or Password")));
    }

    @Test
    public void LogIn_InvalidUsernameAndPassword_LogInFailure() {

        String NewUsername = "fail";
        String NewPassword = "fail";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewInvalid")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Invalid Username or Password")));
    }

    @Test
    public void LogIn_NullUsernameValidPassword_LogInFailure() {

        String NewUsername = "";
        String NewPassword = "Test@123";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewInvalid")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Invalid Username or Password")));
    }

    @Test
    public void LogIn_ValidUsernameNullPassword_LogInFailure() {

        String NewUsername = "test@test.com";
        String NewPassword = "";
        // Type text and then press the button.
        onView(withId(R.id.editTextUsername)).perform(typeText(NewUsername));
        onView(withId(R.id.editTextSignInPassword)).perform(typeText(NewPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonLogIn)).perform(click());

        // Verify the test is displayed in the Ui
        UiObject2 changedText = mDevice
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textViewInvalid")),
                        500 /* wait 500ms */);
        assertThat(changedText.getText(), is(equalTo("Invalid Username or Password")));
    }



    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    public void deleteUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            currentUser.deleteInBackground(e -> {
                if(e==null){
                    //Delete successfull
                }else{
                    // Something went wrong while deleting
                }
            });
        }
    }
}