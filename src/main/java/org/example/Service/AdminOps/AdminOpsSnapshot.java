package org.example.Service.AdminOps;

import org.example.Model.AdminOps.AdminTask;

import java.util.List;

public record AdminOpsSnapshot(
        List<AdminTask> todo,
        List<AdminTask> doing,
        List<AdminTask> done,
        int urgentCount,
        int overdueCount,
        int todayCreatedCount,
        double beforeDeadlineRate,
        AdminRewardSnapshot reward,
        List<AdminTaskAuditEntry> auditEntries
) {
}

