package com.kcs.attendancesystem.service;

import com.kcs.attendancesystem.dto.ResponseVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCount;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountBlockVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountClusterVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountSchoolVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountSchoolWiseVO;

import org.springframework.data.domain.Page;

import java.util.List;

import com.kcs.attendancesystem.dto.AttendanceRequestVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceVO;
import com.kcs.attendancesystem.dto.TeacherCountBlockVO;
import com.kcs.attendancesystem.dto.TeacherCountClusterVO;
import com.kcs.attendancesystem.dto.TeacherCountSchoolVO;
import com.kcs.attendancesystem.dto.TeacherCountSchoolWiseVO;
import com.kcs.attendancesystem.dto.TeacherCountVO;

public interface TeacherAttendanceService {

	ResponseVO<Page<TeacherAttendanceVO>> generateTeacherAttendanceReportMis(AttendanceRequestVO pageRequestVO);

	Page<TeacherAttendanceVO> generateTeacherAttendanceReport(AttendanceRequestVO pageRequestVO);

	ResponseVO<List<TeacherAttendanceCount>> TeacherCountByAttendance();

	ResponseVO<TeacherCountVO> TeacherRegularCount();

	ResponseVO<List<TeacherAttendanceCountBlockVO>> findByDistrict(Long districtId);

	ResponseVO<List<TeacherAttendanceCountClusterVO>> findBySsaBlockCode(Long districtId, Long blockId);

	ResponseVO<List<TeacherAttendanceCountSchoolVO>> findByCluster(Long districtId, Long blockId, Long clusterId);

	ResponseVO<List<TeacherAttendanceCountSchoolWiseVO>> findTeacherBySchoolWise(Long districtId, Long blockId,
			Long clusterId, Long schoolId);

}
