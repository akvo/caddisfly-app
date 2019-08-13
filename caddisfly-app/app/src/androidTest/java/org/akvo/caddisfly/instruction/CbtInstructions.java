/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.instruction;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.ui.TestActivity;
import org.akvo.caddisfly.util.TestHelper;
import org.akvo.caddisfly.util.TestUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CbtInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();
    private final StringBuilder listString = new StringBuilder();

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<TestActivity> mTestActivityRule =
            new ActivityTestRule<>(TestActivity.class, false, false);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    private static void CheckTextInTable(@StringRes int resourceId) {
        ViewInteraction textView3 = onView(
                allOf(withText(resourceId),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText(resourceId)));
    }

//    private static void CheckTextInTable(String text) {
//        ViewInteraction textView3 = onView(
//                allOf(withText(text),
//                        childAtPosition(
//                                childAtPosition(
//                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
//                                        0),
//                                1),
//                        isDisplayed()));
//        textView3.check(matches(withText(text)));
//    }

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityTestRule);
    }

    @Test
    public void cbtInstructions() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(0);

        ViewInteraction textView = onView(
                allOf(withText("www.aquagenx.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("www.aquagenx.com")));

        ViewInteraction button = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textToolbarTitle),
                childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        0)),
                        1),
                isDisplayed()));
//        textView2.check(matches(withText("E.coli – Aquagenx CBT")));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_phase_2),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                1),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.button_phase_2),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                1),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_phase_2),
                        withText(R.string.instructions),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        CheckTextInTable(R.string.prepare_area_put_on_gloves);

//        CheckTextInTable(R.string.open_growth_medium_sachet);

        onView(withContentDescription("1")).check(matches(hasDrawable()));

        ViewInteraction imageView = onView(
                allOf(withContentDescription("1"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.ScrollView.class),
                                        0),
                                6),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.pager_indicator),
                childAtPosition(
                        allOf(withId(R.id.layout_footer),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.RelativeLayout.class),
                                        1)),
                        0),
                isDisplayed()));

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.image_pageRight),
                        childAtPosition(
                                allOf(withId(R.id.layout_footer),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatImageView.perform(click());

//        CheckTextInTable(R.string.dissolve_medium_in_sample);

        onView(withContentDescription("2")).check(matches(hasDrawable()));

        TestUtil.nextPage();

//        CheckTextInTable(getString(R.string.medium_dissolves)
//                + " " + getString(R.string.when_medium_dissolved));

        onView(withContentDescription("3")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        CheckTextInTable(R.string.label_compartment_bag);

        onView(withContentDescription("4")).check(matches(hasDrawable()));

        TestUtil.nextPage(3);

        CheckTextInTable(R.string.let_incubate);

        onView(withContentDescription("7")).check(matches(hasDrawable()));

        onView(withText(R.string.read_instructions)).perform(click());

        onView(withText(R.string.below25Degrees)).check(matches(isDisplayed()));

        onView(withText(R.string.incubate_in_portable)).check(matches(isDisplayed()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        TestUtil.nextPage();

        CheckTextInTable(R.string.take_photo_of_incubated);

        onView(withContentDescription("8")).check(matches(hasDrawable()));

        mDevice.pressBack();

        onView(withId(R.id.button_phase_2)).perform(click());

        CheckTextInTable(R.string.prepare_area_put_on_gloves);

//        CheckTextInTable(R.string.open_growth_medium_sachet);

        onView(withContentDescription("1")).check(matches(hasDrawable()));

        TestUtil.nextPage();

//        CheckTextInTable(R.string.dissolve_medium_in_sample);

        onView(withContentDescription("2")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction button1 = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button1.check(matches(isDisplayed()));

        mActivityTestRule.finishActivity();
    }

    @Test
    @RequiresDevice
    public void testInstructionsCbt() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        List<TestInfo> testList = testConfigRepository.getTests(TestType.CBT);
        for (int i = 0; i < TestConstants.CBT_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.CBT);

            String uuid = testList.get(i).getUuid();

            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("9991fb84dd90 606b771e0ffe 6060e4dbe59d").contains(id))
//
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
                intent.putExtras(data);

                mTestActivityRule.launchActivity(intent);

                int pages = navigateToCbtTest(id);

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");

                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");

                TestHelper.getCurrentActivity().finish();
                mTestActivityRule.finishActivity();
            }

        }

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());
    }

    private int navigateToCbtTest(String id) {

        TestUtil.sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, -1);

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed())).perform(click());

        TestUtil.sleep(1000);

        mDevice.waitForIdle();

//        takeScreenshot(id, 0);
//
//        onView(withText(R.string.test_selected)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(1000);

                takeScreenshot(id, i + 1);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestHelper.navigateUp();
                TestUtil.sleep(300);
                break;
            }
        }
        return pages + 1;
    }

    @Test
    @RequiresDevice
    public void testInstructionsAll() {

        final TestListViewModel viewModel =
                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);

        List<TestInfo> testList = viewModel.getTests(TestType.CBT);

        for (int i = 0; i < TestConstants.CBT_TESTS_COUNT; i++) {
            TestInfo testInfo = testList.get(i);

            String id = testInfo.getUuid();
            id = id.substring(id.lastIndexOf("-") + 1);

            int pages = navigateToTest(i, id);

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

            onView(withText(testInfo.getName())).check(matches(isDisplayed()));

            mDevice.pressBack();

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }

        mActivityTestRule.finishActivity();

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());

    }

    private int navigateToTest(int index, String id) {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(index);

        mDevice.waitForIdle();

        sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withText(R.string.instructions)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            try {
                takeScreenshot(id, pages);

                pages++;

                try {
                    onView(withId(R.id.button_phase_2)).perform(click());
                    sleep(600);
                    takeScreenshot(id, pages);
                    pages++;
                    sleep(600);
                    mDevice.pressBack();
                } catch (Exception ignore) {
                }

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                sleep(600);
                Random random = new Random(Calendar.getInstance().getTimeInMillis());
                if (random.nextBoolean()) {
                    Espresso.pressBack();
                } else {
                    mDevice.pressBack();
                }
                break;
            }
        }
        return pages;
    }
}
