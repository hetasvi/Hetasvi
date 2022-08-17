package com.kcs.attendancesystem.dto;

import lombok.Data;

@Data
public class TeacherAttendanceCountClusterVO {
	private Long districtCode;

	private Long blockCode;

	private String clusterName;

	private Long regular;
	private Long notRegular;

	private Long clusterCode;

}
