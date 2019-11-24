package com.flagcamp.gofitness.controller;


import com.flagcamp.gofitness.model.TraineeReservation;
import com.flagcamp.gofitness.model.Trainee;
import com.flagcamp.gofitness.model.Trainer;

import com.flagcamp.gofitness.service.TraineeService;
import com.flagcamp.gofitness.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/trainee")
@CrossOrigin(origins = "http://localhost:3000")
public class TraineeController {

    @Autowired
    private TraineeService traineeService;
    @Autowired
    private TrainerService trainerService;

    private SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmm");

    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public Trainee getTraineeInfo(HttpServletRequest request) {
        String traineeEmail = (String) request.getAttribute("userEmail");
        return traineeService.findTraineeByEmail(traineeEmail);
    }

    @RequestMapping(value = "/getAllTrainer", method = RequestMethod.GET)
    public List<Trainer> getAllTrainer(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!role.equals("trainee")) {
            return null;
        }
        return trainerService.getAllTrainers();
    }

    @PostMapping(value = "/reserve")
    public Map<String, String> reserveClass(@RequestBody Map<String, String> param, HttpServletRequest request) throws ParseException {
        Map<String, String> map = new HashMap<>();
        String traineeEmail = (String) request.getAttribute("userEmail");
        
        String trainerEmail = param.get("trainer_email");
        String startTime = param.get("start").replaceAll(",", "");
        String endTime = param.get("end").replaceAll(",", "");
        String traineeName = traineeService.getFullName(traineeEmail);
        String trainerName = trainerService.getFullName(trainerEmail);
        traineeService.addTraineeReservation(traineeEmail, trainerEmail, trainerName, startTime, endTime);
        trainerService.addTrainerReservation(trainerEmail, traineeEmail, traineeName, startTime, endTime);
        map.put("status", "OK");
        map.put("msg", "add schedule successful.");
        return map;
    }
    
    @RequestMapping(value = "/getReservation", method = RequestMethod.GET)
    public List<TraineeReservation> getReservation(HttpServletRequest request) throws ParseException {
        String traineeEmail = (String) request.getAttribute("trainee");
        Date date = new Date();
        String now = sf.format(date);
        List<TraineeReservation> list = traineeService.getTraineeReservation(traineeEmail, now);
        return list;
    }
    


    @PostMapping(value = "/cancelReservation")
    public Map<String, String> cancelReservations(@RequestBody Map<String, String> param, HttpServletRequest request) throws ParseException {
    	Map<String, String> map = new HashMap<>();
        HttpSession session = request.getSession();
        if (session == null || session.getAttribute("trainer") == null) {
            map.put("status", "error");
            map.put("msg", "user login expired.");
            return map;
        }
        String traineeEmail = session.getAttribute("trainee").toString();
        String startTime = param.get("start").replaceAll(",", "");
        String endTime = param.get("end").replaceAll(",", "");
        long start = sf.parse(startTime).getTime();
        long end = sf.parse(endTime).getTime();
        long time = 30 * 60 * 1000;
        while (start < end) {
        	traineeService.cancelReservation(traineeEmail, start);
        	start += time;
        }
    	return map;
    }
 
    
    
    
//    public Map<String, String> cancelClass(@RequestBody Map<String, String> param, HttpServletRequest request) throws JSONException {
//        Map<String, String> map = new HashMap<>();
//        HttpSession session = request.getSession();
//        String traineeEmail = session.getAttribute("trainee").toString();
//        if (traineeEmail == null || traineeEmail.length() == 0) {
//            map.put("status", "error");
//            map.put("msg", "user login expired");
//            return map;
//        }
//        String trainerEmail = param.get("trainer_email");
//        String startTime = param.get("start_time");
//        String endTime = param.get("end_time");
//        classService.cancelClass(traineeEmail, trainerEmail, startTime, endTime);
//        map.put("status", "OK");
//        map.put("msg", "You have cancelled the class.");
//        return map;
//    }

}
