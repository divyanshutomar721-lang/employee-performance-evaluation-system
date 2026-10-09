package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.User;
import com.epes.service.report.EmployeeReport;
import com.epes.service.report.ManagerOverview;
import com.epes.service.report.TeamReport;

/**
 * Service interface for performance analytics, summary scorecards, and management reports.
 */
public interface ReportService {

    /**
     * Builds a comprehensive performance report for an individual employee.
     * Accessible by the employee, their supervising manager, or administrators.
     */
    EmployeeReport buildEmployeeReport(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Builds an aggregate report for a team.
     * Accessible by the owning manager, team members, or administrators.
     */
    TeamReport buildTeamReport(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Builds a managerial overview report across all teams and employees managed by the actor.
     * Restricted to managers and administrators.
     */
    ManagerOverview buildManagerOverview(User actor)
            throws AuthorizationException, DataAccessException;
}
