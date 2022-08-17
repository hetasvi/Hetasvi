package com.kcs.attendancesystem.dto;

import lombok.Data;

@Data
public class TeacherAttendanceCountSchoolVO {
	private String schoolName;
	private Long regular;
	private Long notRegular;

	private Long schoolCode;

}
