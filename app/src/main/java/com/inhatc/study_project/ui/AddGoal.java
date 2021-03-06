package com.inhatc.study_project.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.inhatc.study_project.AlarmReceiver;
import com.inhatc.study_project.R;
import com.inhatc.study_project.data.AppDatabase;
import com.inhatc.study_project.data.Goal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddGoal extends AppCompatActivity implements View.OnClickListener {
    private Spinner quantitySpinner;
    private Button btnTimePicker, btnDatePicker1, btnDatePicker2, btnOK;
    private Switch switchButton;
    private CheckBox chkMon, chkTue, chkWed, chkThu, chkFri, chkSat, chkSun;
    private EditText edtGoalName, edtQuantity;
    private int selectYear, selectMon, selectDay, bYear, bMonth, bDay;
    private String selSpinner;
    private Date date;
    private ArrayList<Date> dateList;

    private AlarmManager alarmManager;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgoal);

        chkMon = (CheckBox) findViewById(R.id.chk_mon);
        chkTue = (CheckBox) findViewById(R.id.chk_tue);
        chkWed = (CheckBox) findViewById(R.id.chk_wed);
        chkThu = (CheckBox) findViewById(R.id.chk_thu);
        chkFri = (CheckBox) findViewById(R.id.chk_fri);
        chkSat = (CheckBox) findViewById(R.id.chk_sat);
        chkSun = (CheckBox) findViewById(R.id.chk_sun);
        btnTimePicker = (Button) findViewById(R.id.btnTimePicker);
        btnDatePicker1 = (Button) findViewById(R.id.btnDatePicker1);
        btnDatePicker2 = (Button) findViewById(R.id.btnDatePicker2);
        btnOK = (Button) findViewById(R.id.btnOK);
        switchButton = (Switch) findViewById(R.id.switchButton);
        edtGoalName = (EditText) findViewById(R.id.edt_goalName);
        edtQuantity = (EditText) findViewById(R.id.edt_quantity);

        // ????????? editText??? ????????? ??????
        edtGoalName.post(new Runnable() {
            @Override
            public void run() {
                edtGoalName.setFocusableInTouchMode(true);
                edtGoalName.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtGoalName, 0);
            }
        });

        // ????????? ??????
        String[] items = getResources().getStringArray(R.array.quantities);
        quantitySpinner = (Spinner)findViewById(R.id.quantityLists);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items);
        quantitySpinner.setAdapter(adapter);
        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selSpinner = items[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // TimePicker ?????????
        btnTimePicker.setOnClickListener(this);

        // ????????? ?????????/????????????
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {        // ????????? ????????? ????????? ????????????
                    btnDatePicker1.setEnabled(true);
                    btnDatePicker2.setEnabled(true);
                    btnDatePicker1.setTextColor(Color.parseColor("#000000"));
                    btnDatePicker2.setTextColor(Color.parseColor("#000000"));
                    chkMon.setEnabled(true);
                    chkTue.setEnabled(true);
                    chkWed.setEnabled(true);
                    chkThu.setEnabled(true);
                    chkFri.setEnabled(true);
                    chkSat.setEnabled(true);
                    chkSun.setEnabled(true);
                } else {
                    btnDatePicker1.setEnabled(false);
                    btnDatePicker2.setEnabled(false);
                    btnDatePicker1.setTextColor(Color.parseColor("#9F9F9F"));
                    btnDatePicker2.setTextColor(Color.parseColor("#9F9F9F"));
                    chkMon.setEnabled(false);
                    chkTue.setEnabled(false);
                    chkWed.setEnabled(false);
                    chkThu.setEnabled(false);
                    chkFri.setEnabled(false);
                    chkSat.setEnabled(false);
                    chkSun.setEnabled(false);
                }
            }
        });
        btnDatePicker1.setOnClickListener(this);
        btnDatePicker2.setOnClickListener(this);
        btnOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Calendar?????? ?????? ????????? ?????? ??????
        Intent intent = getIntent();
        selectYear = intent.getIntExtra("selectYear", 0);
        selectMon = intent.getIntExtra("selectMonth", 0);
        selectDay = intent.getIntExtra("selectDay", 0);

        // DB ??????
        AppDatabase db = AppDatabase.getAppDatabase(this);
        
        switch (v.getId()) {
            case R.id.btnTimePicker :
                TimePickerDialog timeDialog = new TimePickerDialog(AddGoal.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        btnTimePicker.setText(String.format("%02d : %02d", hourOfDay, minute));
                    }
                }, 0, 0, false);
                timeDialog.show();
                break;
            case R.id.btnDatePicker1 :
                DatePickerDialog datePicker1 = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                btnDatePicker1.setText(String.format("%02d/%02d/%02d", year, month+1, dayOfMonth));
                                bYear = year;
                                bMonth = month;
                                bDay = dayOfMonth;
                            }
                        }, selectYear, selectMon, selectDay);
                datePicker1.show();
                break;
            case R.id.btnDatePicker2 :
                if(btnDatePicker1.getText().toString().equals("?????? ?????? ??????")) {
                    Toast.makeText(this, "?????? ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    break;
                }
                DatePickerDialog datePicker2 = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                btnDatePicker2.setText(String.format("%02d/%02d/%02d", year, month+1, dayOfMonth));
                            }
                        }, bYear, bMonth, bDay+1);
                Calendar minDate = Calendar.getInstance();
                minDate.set(bYear, bMonth, bDay+1);
                datePicker2.getDatePicker().setMinDate(minDate.getTimeInMillis());
                datePicker2.show();
                break;
            case R.id.btnOK:                                // ?????? ?????? ????????? ???
                // ??????????????? ??????????????? ????????????
                try {
                    // EditText ?????? ?????? ??????
                    if(edtGoalName.getText().toString().matches("") || edtQuantity.getText().toString().matches("")) {
                        Toast.makeText(AddGoal.this, "???????????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(Integer.parseInt(edtQuantity.getText().toString()) <= 0) {
                        Toast.makeText(AddGoal.this, "?????? ?????? ????????? 0?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // ?????? ?????? ????????? ????????? ?????????????????? ??????
                    Integer.parseInt(edtQuantity.getText().toString());
                    // ?????? ??????
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    date = dateFormat.parse(selectYear+"-"+(selectMon+1)+"-"+selectDay);
                } catch(NumberFormatException numEx) {
                    Toast.makeText(AddGoal.this, "?????? ?????? ????????? ????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                    return;
                } catch(ParseException pEx) {
                    Log.d("AddGoal", pEx.toString());
                } catch(Exception e) {
                    Toast.makeText(AddGoal.this, "????????? ??????????????????. ?????? ??? ??????????????????.", Toast.LENGTH_SHORT).show();
                    Log.d("AddGoal", e.toString());
                }

                // ????????? ????????? DB??? ????????????
                if(switchButton.isChecked()) {      // ????????? ????????? ???????????? ????????? ??????
                    if (btnDatePicker1.getText().equals("?????? ?????? ??????") || btnDatePicker2.getText().equals("?????? ?????? ??????")) {
                        Toast.makeText(AddGoal.this, "?????? ????????? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    dateList = new ArrayList<Date>();
                    String[] startDate = btnDatePicker1.getText().toString().split("/");
                    Calendar startCal = Calendar.getInstance(); startCal.set(Integer.parseInt(startDate[0]), Integer.parseInt(startDate[1])-1, Integer.parseInt(startDate[2])-1, 0, 0, 0);
                    String[] endDate = btnDatePicker2.getText().toString().split("/");
                    Calendar endCal = Calendar.getInstance(); endCal.set(Integer.parseInt(endDate[0]), Integer.parseInt(endDate[1])-1, Integer.parseInt(endDate[2])+1, 0, 0, 0);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Integer.parseInt(startDate[0]), Integer.parseInt(startDate[1])-1, Integer.parseInt(startDate[2]), 0, 0, 0);
                    // ????????? ?????? ????????? ?????????
                    while (endCal.compareTo(cal) > 0) {
                        if (chkMon.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            addDateList(cal, startCal, endCal);
                        } if (chkTue.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                            addDateList(cal, startCal, endCal);
                        }  if (chkWed.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                            addDateList(cal, startCal, endCal);
                        }  if (chkThu.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                            addDateList(cal, startCal, endCal);
                        }  if (chkFri.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                            addDateList(cal, startCal, endCal);
                        }  if (chkSat.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                            addDateList(cal, startCal, endCal);
                        }  if (chkSun.isChecked()) {
                            cal.getTime();
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            addDateList(cal, startCal, endCal);
                        }
                        cal.add(cal.DAY_OF_MONTH, 7);
                        cal.getTime();
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    }
                    if(dateList.size() == 0) {
                        Toast.makeText(AddGoal.this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    new Thread(() -> {
                        for (int i=0; i<dateList.size(); i++) {
                            Goal goal = convertGoal(dateList.get(i));
                            db.goalDao().insert(goal);
                        }
                    }).start();
                    Toast.makeText(AddGoal.this, "????????? ????????????????????? :)", Toast.LENGTH_SHORT).show();
                } else {
                    // ????????? ????????? DB??? ??????
                    new Thread(() -> {
                        Goal goal = convertGoal(date);
                        db.goalDao().insert(goal);
                    }).start();
                    Toast.makeText(AddGoal.this, "????????? ????????????????????? :)", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
    }

    public Goal convertGoal(Date date) {
        Goal goal = new Goal();
        goal.setGoalName(edtGoalName.getText().toString());
        goal.setGoalDate(date);
        goal.setGoalTime(btnTimePicker.getText().toString());
        goal.setQuantity(Integer.parseInt(edtQuantity.getText().toString()));
        goal.setRangeValue(selSpinner);
        return goal;
    }
    
    public void addDateList(Calendar cal, Calendar startCal, Calendar endCal) {
        if(cal.compareTo(startCal) > 0 && endCal.compareTo(cal) > 0) {
            try {
                String calDay = String.format("%04d-%02d-%02d", cal.getTime().getYear() + 1900, cal.getTime().getMonth() + 1, cal.getTime().getDate());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date dCalDay = dateFormat.parse(calDay);
                dateList.add(dCalDay);
            } catch (ParseException pEx) {
                Log.d("FragmentCalendar", pEx.toString());
            } catch (Exception e) {
                Log.d("FragmentCalendar", e.toString());
            }
        }
    }

    private void setAlarm() {
        // AlarmReceiver??? ??? ??????
        Intent receiverIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, receiverIntent, 0);

        String from = "2021-05-25 11:00:00";        // ?????? ??????
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime = null;
        try {
            dateTime = sdf.parse(from);
        } catch(ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}