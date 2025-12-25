package com.nti.nice_gallery.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.LocaleData;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfSettings_Test1;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelMediaTreeItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ActivityFilters extends AppCompatActivity {
    private ChipGroup extensions;
    private ChipGroup types;
    EditText minWeight;
    EditText maxWeight;
    Button minCreateDate;
    Button maxCreateDate;
    DateTimeFormatter dateFormatter;
    Button duration;
    int hour,minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        dateFormatter=DateTimeFormatter.ofPattern(getString(R.string.format_java_simple_date_full_numeric));
        minCreateDate=findViewById(R.id.minCreateDate);
        maxCreateDate=findViewById(R.id.maxCreateDate);
        minCreateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    minCreateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });
        maxCreateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    maxCreateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });

        duration=findViewById(R.id.duration);
        duration.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener=new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    hour=selectedHour;
                    minute=selectedMinute;
                    duration.setText(String.format(Locale.getDefault(),getString(R.string.format_duration_short),hour,minute));
                }
            };
            TimePickerDialog dialog=new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
            dialog.show();
        });

        Button saveButton=findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            ModelFilters resultModel=new ModelFilters();

            types= findViewById(R.id.types);
            List<ModelMediaTreeItem.Type> resultTypes=new ArrayList<>();
            for(int itemId:types.getCheckedChipIds()){
                Chip chip=findViewById(itemId);
                ModelMediaTreeItem.Type resultType=ModelMediaTreeItem.Type.valueOf(chip.getText().toString());
                resultTypes.add(resultType);
            }
            resultModel.types=resultTypes;

            extensions=findViewById(R.id.extensions);
            List<String> resultExtensions=new ArrayList<>();
            for(int itemId:extensions.getCheckedChipIds()){
                Chip chip=findViewById(itemId);
                String resultExtension = chip.getText().toString();
                resultExtensions.add(resultExtension);
            }
            resultModel.extensions=resultExtensions;

            minWeight=findViewById(R.id.minWeight);
            resultModel.minWeight=Long.parseLong(minWeight.getText().toString());

            maxWeight=findViewById(R.id.maxWeight);
            resultModel.maxWeight=Long.parseLong(maxWeight.getText().toString());

            resultModel.minCreateDate=LocalDate.parse(maxCreateDate.getText().toString(),dateFormatter);
            resultModel.maxCreateDate=LocalDate.parse(maxCreateDate.getText().toString(),dateFormatter);

            duration=findViewById(R.id.duration);
            resultModel.duration=Integer.parseInt(duration.getText().toString().split(":")[0])*60+
                    Integer.parseInt(duration.getText().toString().split(":")[1]);

            IManagerOfSettings managerOfSettings = new ManagerOfSettings_Test1(this);
            managerOfSettings.saveFilters(resultModel);
            Toast.makeText(this,"Настройки успешно сохранены",Toast.LENGTH_SHORT).show();
        });

        Button resetButton=findViewById(R.id.resetButton);
        resetButton.setOnClickListener(view -> {
            IManagerOfSettings managerOfSettings = new ManagerOfSettings_Test1(this);
            managerOfSettings.saveFilters(null);
            Toast.makeText(this,"Настройки успешно сброшены",Toast.LENGTH_SHORT).show();
        });
    }
}
