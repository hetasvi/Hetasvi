package com.kcs.attendancesystem.dto;

import lombok.Data;

@Data
public class TeacherAttendanceCountBlockVO {

	private Long districtCode;

	private String blockName;

	private Long regular;
	private Long notRegular;

	private Long blockCode;

}
