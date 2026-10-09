package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.ValidationException;
import com.epes.model.Feedback;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FeedbackService}.
 */
class FeedbackServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Manager submits feedback, employee and manager retrieve it, creator deletes it")
    void testFeedbackLifecycle() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Feedback Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Feedback Emp", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Feedback Team",
                List.of(employee.getId()), Map.of());

        Feedback fb = appContext.getFeedbackService().giveFeedback(
                manager, employee.getId(), null, "Excellent presentation in sprint review", 5);
        assertNotNull(fb.getId());
        assertEquals(5, fb.getRating());

        // Employee can list their feedback
        List<Feedback> empList = appContext.getFeedbackService().listForEmployee(employee, employee.getId());
        assertEquals(1, empList.size());
        assertEquals("Excellent presentation in sprint review", empList.get(0).getComments());

        // Manager can list feedback given by them
        List<Feedback> mgrList = appContext.getFeedbackService().listGivenByMe(manager);
        assertEquals(1, mgrList.size());

        // Manager deletes feedback
        appContext.getFeedbackService().deleteFeedback(manager, fb.getId());
        assertTrue(appContext.getFeedbackService().listForEmployee(employee, employee.getId()).isEmpty());
    }

    @Test
    @DisplayName("Unsupervised manager submitting feedback is rejected")
    void testUnsupervisedFeedbackRejected() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Unrelated Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Unrelated Emp", Role.EMPLOYEE, true, null));

        assertThrows(AuthorizationException.class, () ->
                appContext.getFeedbackService().giveFeedback(manager, employee.getId(), null, "Comment", 4));
    }

    @Test
    @DisplayName("Feedback with invalid ratings or blank comments throws ValidationException")
    void testFeedbackValidation() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Val Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Val Emp", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Val Team",
                List.of(employee.getId()), Map.of());

        assertThrows(ValidationException.class, () ->
                appContext.getFeedbackService().giveFeedback(manager, employee.getId(), null, "", 5));

        assertThrows(ValidationException.class, () ->
                appContext.getFeedbackService().giveFeedback(manager, employee.getId(), null, "Valid comment", 6));
    }
}
