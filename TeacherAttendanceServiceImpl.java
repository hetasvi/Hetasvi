package com.kcs.attendancesystem.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.kcs.attendancesystem.dto.ResponseVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCount;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountBlockVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountClusterVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountSchoolVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceCountSchoolWiseVO;
import com.kcs.attendancesystem.enums.ResponseCode;
import com.kcs.attendancesystem.utility.Utility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.kcs.attendancesystem.core.SSABlock;
import com.kcs.attendancesystem.core.Teacher;
import com.kcs.attendancesystem.core.TeacherAttendance;
import com.kcs.attendancesystem.dto.AttendanceRequestVO;
import com.kcs.attendancesystem.dto.TeacherAttendanceVO;
import com.kcs.attendancesystem.dto.TeacherCountBlockVO;
import com.kcs.attendancesystem.dto.TeacherCountVO;
import com.kcs.attendancesystem.enums.AttendanceStatus;
import com.kcs.attendancesystem.repository.SSABlockRepository;
import com.kcs.attendancesystem.repository.SSAClustersRepository;
import com.kcs.attendancesystem.repository.SSADistrictRepository;
import com.kcs.attendancesystem.repository.SSASchoolRepository;
import com.kcs.attendancesystem.repository.TeacherAttendanceRepository;
import com.kcs.attendancesystem.repository.TeacherRepository;
import com.kcs.attendancesystem.service.TeacherAttendanceService;
import com.kcs.attendancesystem.utils.Utils;

@Service
public class TeacherAttendanceServiceImpl implements TeacherAttendanceService {

	@Autowired
	private TeacherAttendanceRepository teacherAttendanceRepository;

	@Autowired
	private TeacherRepository teacherRepository;

	@Autowired
	private SSADistrictRepository ssaDistrictRepository;

	@Autowired
	private SSABlockRepository ssaBlockRepository;

	@Autowired
	private SSAClustersRepository ssaClustersRepository;

	@Autowired
	private SSASchoolRepository ssaSchoolRepository;

	public void addAbsentEntry() {

		Date date = new Date();

		List<Teacher> teachers = teacherRepository.findAll();

		List<TeacherAttendance> teacherAttendance = teacherAttendanceRepository.findByAtDate(date);

		Map<Teacher, List<TeacherAttendance>> teacherAttMap = teacherAttendance.stream()
				.collect(Collectors.groupingBy(TeacherAttendance::getTeacher));

		for (Teacher teacher : teachers) {
			if (!teacherAttMap.containsKey(teacher)) {
				TeacherAttendance teacherAtt = new TeacherAttendance();
				teacherAtt.setTeacher(teacher);
				teacherAtt.setAttendanceDate(date);
				teacherAtt.setStatus(AttendanceStatus.ABSENT);
				teacherAtt.setIsProcessed(true);
				teacherAttendanceRepository.save(teacherAtt);
			}
		}
	}

	@Override
	public ResponseVO<Page<TeacherAttendanceVO>> generateTeacherAttendanceReportMis(AttendanceRequestVO pageRequestVO) {

		Page<TeacherAttendance> teacherAttPage = getTeacherAttendanceData(pageRequestVO);

		Page<TeacherAttendanceVO> teacherVOPage = teacherAttPage.map(this::convertToVO);

		return ResponseVO.create(ResponseCode.SUCCESSFUL.getId(), ResponseCode.SUCCESSFUL.getName(),
				ResponseCode.SUCCESSFUL.getName(), teacherVOPage);
	}

	@Override
	public Page<TeacherAttendanceVO> generateTeacherAttendanceReport(AttendanceRequestVO pageRequestVO) {

		Page<TeacherAttendance> teacherAttPage = getTeacherAttendanceData(pageRequestVO);

		return teacherAttPage.map(this::convertToVO);
	}

	private Page<TeacherAttendance> getTeacherAttendanceData(AttendanceRequestVO pageRequestVO) {
		PageRequest pageRequest = PageRequest.of(pageRequestVO.getPageNo(), pageRequestVO.getPageSize(),
				Sort.by(Sort.Direction.DESC, "id"));

		Page<TeacherAttendance> teacherAttPage = null;

		if (StringUtils.isNotBlank(pageRequestVO.getFromDate()) && StringUtils.isNotBlank(pageRequestVO.getToDate())) {
			teacherAttPage = teacherAttendanceRepository.findByAtDateBetween(
					Utils.parseDate(pageRequestVO.getFromDate()), Utils.parseDate(pageRequestVO.getToDate()),
					pageRequest);
			if (StringUtils.isNotBlank(pageRequestVO.getEmployeeId())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherIdAndDate(
						Long.parseLong(pageRequestVO.getEmployeeId()), Utils.parseDate(pageRequestVO.getFromDate()),
						Utils.parseDate(pageRequestVO.getToDate()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaDistrict())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherSSADistrictAndDate(
						Long.parseLong(pageRequestVO.getSsaDistrict()), Utils.parseDate(pageRequestVO.getFromDate()),
						Utils.parseDate(pageRequestVO.getToDate()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaBlock())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherSSABlockAndDate(
						Long.parseLong(pageRequestVO.getSsaBlock()), Utils.parseDate(pageRequestVO.getFromDate()),
						Utils.parseDate(pageRequestVO.getToDate()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaCluster())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherSSAClusterAndDate(
						Long.parseLong(pageRequestVO.getSsaCluster()), Utils.parseDate(pageRequestVO.getFromDate()),
						Utils.parseDate(pageRequestVO.getToDate()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaSchool())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherSSASchoolAndDate(
						Long.parseLong(pageRequestVO.getSsaSchool()), Utils.parseDate(pageRequestVO.getFromDate()),
						Utils.parseDate(pageRequestVO.getToDate()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getDesignation())) {
				teacherAttPage = teacherAttendanceRepository.findByDesignationAndDate(pageRequestVO.getDesignation(),
						Utils.parseDate(pageRequestVO.getFromDate()), Utils.parseDate(pageRequestVO.getToDate()),
						pageRequest);
			}
		}
		if (StringUtils.isBlank(pageRequestVO.getFromDate()) && StringUtils.isBlank(pageRequestVO.getToDate())) {
			teacherAttPage = teacherAttendanceRepository.findAll(pageRequest);
			if (StringUtils.isNotBlank(pageRequestVO.getUserId())) {
				teacherAttPage = teacherAttendanceRepository.findByUserId(Long.parseLong(pageRequestVO.getUserId()),
						pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getEmployeeId())) {
				teacherAttPage = teacherAttendanceRepository.findByTeacherId(pageRequestVO.getEmployeeId(),
						pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaDistrict())) {
				teacherAttPage = teacherAttendanceRepository
						.findBySSADistrict(Long.parseLong(pageRequestVO.getSsaDistrict()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaBlock())) {
				teacherAttPage = teacherAttendanceRepository.findBySSABlock(Long.parseLong(pageRequestVO.getSsaBlock()),
						pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaCluster())) {
				teacherAttPage = teacherAttendanceRepository
						.findBySSACluster(Long.parseLong(pageRequestVO.getSsaCluster()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getSsaSchool())) {
				teacherAttPage = teacherAttendanceRepository
						.findBySSASchool(Long.parseLong(pageRequestVO.getSsaSchool()), pageRequest);
			}
			if (StringUtils.isNotBlank(pageRequestVO.getDesignation())) {
				teacherAttPage = teacherAttendanceRepository.findByDesignation(pageRequestVO.getDesignation(),
						pageRequest);
			}
		}
		return teacherAttPage;
	}

	@Override
	public ResponseVO<List<TeacherAttendanceCount>> TeacherCountByAttendance() {
		try {
			Set<Date> dates = new HashSet<>();
			Set<TeacherAttendanceCount> list = new HashSet<>();
			List<TeacherAttendanceCount> teacherList = new ArrayList<>();
			List<TeacherAttendance> all = teacherAttendanceRepository.findAll();
			// all.forEach(e -> dates.add(e.getAttendanceDate()));
			for (TeacherAttendance teacherAttendance : all) {
				dates.add(teacherAttendance.getAttendanceDate());
			}
			for (Date date : dates) {
				List<TeacherAttendance> attendanceList = new ArrayList<>();
				attendanceList = teacherAttendanceRepository.findByAtDateBetween(date, date);
				Map<Long, TeacherAttendance> map = new LinkedHashMap<>();
				for (TeacherAttendance convert : attendanceList) {
					map.put(convert.getId(), convert);
				}
				for (Map.Entry<Long, TeacherAttendance> entry : map.entrySet()) {
					Long i = map.entrySet().parallelStream().filter(
							e -> e.getValue().getTeacher().getId().equals(entry.getValue().getTeacher().getId()))
							.count();
					if (i > 2) {
						List<String> time = new ArrayList<>();
						for (TeacherAttendance teacherAttendance : attendanceList) {
							if (teacherAttendance.getTeacher().getId().equals(entry.getValue().getTeacher().getId())) {
								if (teacherAttendance.getAttendanceDate().getHours() == 0
										&& teacherAttendance.getAttendanceDate().getMinutes() == 0
										&& teacherAttendance.getAttendanceDate().getSeconds() == 0) {
									time.add(teacherAttendance.getCreatedDate().toString().substring(11, 19));
								} else {
									time.add(teacherAttendance.getAttendanceDate().toString().substring(11, 19));
								}
							}
						}
						TeacherAttendanceCount teacherAttendanceCount = new TeacherAttendanceCount();
						teacherAttendanceCount.setTeacherId(entry.getValue().getTeacher().getId());
						teacherAttendanceCount.setCount(i);
						LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						teacherAttendanceCount.setDate(localDate);
						teacherAttendanceCount.setTime(time);
						teacherAttendanceCount.setTeacherName(entry.getValue().getTeacher().getFirstName());
						list.add(teacherAttendanceCount);
					}
				}
			}
			// get Comparator using Lambda expression
			Comparator<TeacherAttendanceCount> comparatorAsc = (prod1, prod2) -> prod1.getDate()
					.compareTo(prod2.getDate());
			List<TeacherAttendanceCount> a = new ArrayList<>(list);
			Collections.sort(a, comparatorAsc);
			for (TeacherAttendanceCount teacherAttendanceCount : a) {
				if (!teacherList.parallelStream()
						.anyMatch(e -> e.getTeacherId().equals(teacherAttendanceCount.getTeacherId()))) {
					teacherList.add(teacherAttendanceCount);
				}
			}
			return ResponseVO.create(ResponseCode.SUCCESSFUL.getId(), ResponseCode.SUCCESSFUL.getName(),
					ResponseCode.SUCCESSFUL.getName(), a);
		} catch (Exception e) {
			return ResponseVO.create(ResponseCode.FAIL.getId(), ResponseCode.FAIL.getName(),
					ResponseCode.FAIL.getName(), null);
		}
	}

	@Override
	public ResponseVO<TeacherCountVO> TeacherRegularCount() {
		try {
			Set<Date> dates = new HashSet<>();
			Set<TeacherAttendanceCount> list = new HashSet<>();
			Set<TeacherAttendanceCount> NotRegularlist = new HashSet<>();
			List<TeacherAttendance> all = teacherAttendanceRepository.findAll();
			// all.forEach(e -> dates.add(e.getAttendanceDate()));
			for (TeacherAttendance teacherAttendance : all) {
				dates.add(teacherAttendance.getAttendanceDate());
			}
			for (Date date : dates) {
				List<TeacherAttendance> attendanceList = new ArrayList<>();
				attendanceList = teacherAttendanceRepository.findByAtDateBetween(date, date);
				Map<Long, TeacherAttendance> map = new LinkedHashMap<>();
				for (TeacherAttendance convert : attendanceList) {
					map.put(convert.getId(), convert);
				}
				for (Map.Entry<Long, TeacherAttendance> entry : map.entrySet()) {
					Long i = map.entrySet().parallelStream().filter(
							e -> e.getValue().getTeacher().getId().equals(entry.getValue().getTeacher().getId()))
							.count();
					if (i > 2) {
						List<String> time = new ArrayList<>();
						for (TeacherAttendance teacherAttendance : attendanceList) {
							if (teacherAttendance.getTeacher().getId().equals(entry.getValue().getTeacher().getId())) {
								if (teacherAttendance.getAttendanceDate().getHours() == 0
										&& teacherAttendance.getAttendanceDate().getMinutes() == 0
										&& teacherAttendance.getAttendanceDate().getSeconds() == 0) {
									time.add(teacherAttendance.getCreatedDate().toString().substring(11, 19));
								} else {
									time.add(teacherAttendance.getAttendanceDate().toString().substring(11, 19));
								}
							}
						}
						TeacherAttendanceCount teacherAttendanceCount = new TeacherAttendanceCount();
						teacherAttendanceCount.setTeacherId(entry.getValue().getTeacher().getId());
						teacherAttendanceCount.setCount(i);
						LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						teacherAttendanceCount.setDate(localDate);
						teacherAttendanceCount.setTime(time);
						teacherAttendanceCount.setTeacherName(entry.getValue().getTeacher().getFirstName());
						list.add(teacherAttendanceCount);
					} else {
						List<String> time = new ArrayList<>();
						for (TeacherAttendance teacherAttendance : attendanceList) {
							if (teacherAttendance.getTeacher().getId().equals(entry.getValue().getTeacher().getId())) {
								if (teacherAttendance.getAttendanceDate().getHours() == 0
										&& teacherAttendance.getAttendanceDate().getMinutes() == 0
										&& teacherAttendance.getAttendanceDate().getSeconds() == 0) {
									time.add(teacherAttendance.getCreatedDate().toString().substring(11, 19));
								} else {
									time.add(teacherAttendance.getAttendanceDate().toString().substring(11, 19));
								}
							}
						}
						TeacherAttendanceCount teacherAttendanceCount = new TeacherAttendanceCount();
						teacherAttendanceCount.setTeacherId(entry.getValue().getTeacher().getId());
						teacherAttendanceCount.setCount(i);
						LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						teacherAttendanceCount.setDate(localDate);
						teacherAttendanceCount.setTime(time);
						teacherAttendanceCount.setTeacherName(entry.getValue().getTeacher().getFirstName());
						NotRegularlist.add(teacherAttendanceCount);
					}
				}
			}
			TeacherCountVO teacherCountVO = new TeacherCountVO();
			teacherCountVO.setRegular(Long.valueOf(list.size()));
			teacherCountVO.setNotRegular(Long.valueOf(NotRegularlist.size()));
			return ResponseVO.create(ResponseCode.SUCCESSFUL.getId(), ResponseCode.SUCCESSFUL.getName(),
					ResponseCode.SUCCESSFUL.getName(), teacherCountVO);
		} catch (Exception e) {
			return ResponseVO.create(ResponseCode.FAIL.getId(), ResponseCode.FAIL.getName(),
					ResponseCode.FAIL.getName(), null);
		}
	}

	private TeacherAttendanceVO convertToVO(TeacherAttendance teacherAttendance) {
		TeacherAttendanceVO vo = new TeacherAttendanceVO();

		vo.setId(teacherAttendance.getId());
		vo.setTeacherName(Utility.generateFullName(teacherAttendance.getTeacher()));
		vo.setAttendanceDate(Utils.formatDateTime(teacherAttendance.getAttendanceDate()));
		vo.setAttendanceStatus(teacherAttendance.getStatus().name());
		vo.setAccuracy(StringUtils.defaultString(teacherAttendance.getPersonAccuracy(), ""));
		vo.setLongitude(teacherAttendance.getLongitude());
		vo.setLatitude(teacherAttendance.getLatitude());
		vo.setCreatedDate(Utils.formatDateTime(teacherAttendance.getCreatedDate()));
		vo.setUpdatedDate(Utils.formatDateTime(teacherAttendance.getLastUpdatedDate()));
		vo.setTransactionId(teacherAttendance.getTransactionId());
		vo.setDeviceId(teacherAttendance.getDeviceId());
		vo.setSchoolAddress(teacherAttendance.getAddress());
		return vo;
	}

	@Override
	public ResponseVO<List<TeacherAttendanceCountBlockVO>> findByDistrict(Long districtId) {
		try {

			Set<Date> dates = new HashSet<>();
			Set<TeacherAttendanceCount> list = new HashSet<>();
			Set<TeacherAttendanceCount> NotRegularlist = new HashSet<>();
			List<TeacherAttendanceCountBlockVO> finalList = new ArrayList<>();

			TeacherAttendanceCountBlockVO teacherAttendanceCountBlockVO = new TeacherAttendanceCountBlockVO();

			List<SSABlock> blocks = ssaBlockRepository.findByDistrictId(districtId);
			for (SSABlock ssaBlock : blocks) {
				List<Teacher> teachers = teacherRepository.findAllBySsaBlockCode(ssaBlock.getBlockCode());
				for (Teacher teacher : teachers) {
					List<TeacherAttendance> tList = teacherAttendanceRepository.findByTeacher(teacher.getId());
					for (TeacherAttendance tt : tList) {
						dates.add(tt.getAttendanceDate());

					}
				}
				for (Date date : dates) {
					List<TeacherAttendance> attendanceList = new ArrayList<>();
					attendanceList = teacherAttendanceRepository.findByAtDateBetween(date, date);
					Map<Long, TeacherAttendance> map = new LinkedHashMap<>();
					for (TeacherAttendance convert : attendanceList) {
						map.put(convert.getId(), convert);
					}
					for (Map.Entry<Long, TeacherAttendance> entry : map.entrySet()) {
						Long i = map.entrySet().parallelStream().filter(
								e -> e.getValue().getTeacher().getId().equals(entry.getValue().getTeacher().getId()))
								.count();
						if (i > 2) {
							List<String> time = new ArrayList<>();
							for (TeacherAttendance teacherAttendance : attendanceList) 
							{
								if (teacherAttendance.getTeacher().getId()
										.equals(entry.getValue().getTeacher().getId())) {
									if (teacherAttendance.getAttendanceDate().getHours() == 0
											&& teacherAttendance.getAttendanceDate().getMinutes() == 0
											&& teacherAttendance.getAttendanceDate().getSeconds() == 0) {
										time.add(teacherAttendance.getCreatedDate().toString().substring(11, 19));
									} else {
										time.add(teacherAttendance.getAttendanceDate().toString().substring(11, 19));
									}
								}
							}
							TeacherAttendanceCount teacherAttendanceCount = new TeacherAttendanceCount();
							teacherAttendanceCount.setTeacherId(entry.getValue().getTeacher().getId());
							teacherAttendanceCount.setCount(i);
							LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							teacherAttendanceCount.setDate(localDate);
							teacherAttendanceCount.setTime(time);
							teacherAttendanceCount.setTeacherName(entry.getValue().getTeacher().getFirstName());
							list.add(teacherAttendanceCount);
						} else {
							List<String> time = new ArrayList<>();
							for (TeacherAttendance teacherAttendance : attendanceList) {
								if (teacherAttendance.getTeacher().getId()
										.equals(entry.getValue().getTeacher().getId())) {
									if (teacherAttendance.getAttendanceDate().getHours() == 0
											&& teacherAttendance.getAttendanceDate().getMinutes() == 0
											&& teacherAttendance.getAttendanceDate().getSeconds() == 0) {
										time.add(teacherAttendance.getCreatedDate().toString().substring(11, 19));
									} else {
										time.add(teacherAttendance.getAttendanceDate().toString().substring(11, 19));
									}
								}
							}
							TeacherAttendanceCount teacherAttendanceCount = new TeacherAttendanceCount();
							teacherAttendanceCount.setTeacherId(entry.getValue().getTeacher().getId());
							teacherAttendanceCount.setCount(i);
							LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							teacherAttendanceCount.setDate(localDate);
							teacherAttendanceCount.setTime(time);
							teacherAttendanceCount.setTeacherName(entry.getValue().getTeacher().getFirstName());
							NotRegularlist.add(teacherAttendanceCount);
						}
					}
				}

				teacherAttendanceCountBlockVO.setBlockCode(ssaBlock.getBlockCode());
				teacherAttendanceCountBlockVO.setBlockName(ssaBlock.getBlockName());
				teacherAttendanceCountBlockVO.setDistrictCode(districtId);
				teacherAttendanceCountBlockVO.setRegular(Long.valueOf(list.size()));
				teacherAttendanceCountBlockVO.setNotRegular(Long.valueOf(NotRegularlist.size()));
				finalList.add(teacherAttendanceCountBlockVO);
			}

			return ResponseVO.create(ResponseCode.SUCCESSFUL.getId(), ResponseCode.SUCCESSFUL.getName(),
					ResponseCode.SUCCESSFUL.getName(), finalList);

		} catch (Exception e) {

			return ResponseVO.create(ResponseCode.FAIL.getId(), ResponseCode.FAIL.getName(),
					ResponseCode.FAIL.getName(), null);
		}
	}

	@Override
	public ResponseVO<List<TeacherAttendanceCountClusterVO>> findBySsaBlockCode(Long districtId, Long blockId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseVO<List<TeacherAttendanceCountSchoolVO>> findByCluster(Long districtId, Long blockId,
			Long clusterId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseVO<List<TeacherAttendanceCountSchoolWiseVO>> findTeacherBySchoolWise(Long districtId, Long blockId,
			Long clusterId, Long schoolId) {
		// TODO Auto-generated method stub
		return null;
	}
}
