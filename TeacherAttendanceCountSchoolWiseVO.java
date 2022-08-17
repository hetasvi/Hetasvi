package com.kcs.attendancesystem.dto;

import java.util.List;

import lombok.Data;

@Data
public class TeacherAttendanceCountSchoolWiseVO {
	private String schoolName;

	private Long regular;
	private Long notRegular;

	private Long schoolCode;

	private List<TeacherVO> teachers;

}
