package com.nti.nice_gallery.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfSettings_Test1;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            types = findViewById(R.id.types);
            List<ModelMediaFile.Type> resultTypes = new ArrayList<>();
            for (int itemId : types.getCheckedChipIds()) {
                Chip chip = findViewById(itemId);
                ModelMediaFile.Type resultType = ModelMediaFile.Type.valueOf(chip.getText().toString());
                resultTypes.add(resultType);
            }
            List<ModelMediaFile.Type> typesValue = resultTypes;

            extensions = findViewById(R.id.extensions);
            List<String> resultExtensions = new ArrayList<>();
            for (int itemId : extensions.getCheckedChipIds()) {
                Chip chip = findViewById(itemId);
                String resultExtension = chip.getText().toString();
                resultExtensions.add(resultExtension);
            }
            List<String> extensionsValue = resultExtensions;

            minWeight = findViewById(R.id.minWeight);
            Long minWeightValue = null;
            if (!minWeight.getText().toString().isEmpty()) {
                minWeightValue = Long.parseLong(minWeight.getText().toString());
            }

            maxWeight = findViewById(R.id.maxWeight);
            Long maxWeightValue = null;
            if (!maxWeight.getText().toString().isEmpty()) {
                maxWeightValue = Long.parseLong(maxWeight.getText().toString());
            }

            LocalDateTime minCreateAtValue = null;
            if (!minCreateDate.getText().toString().equals("Начальная дата")) {
                minCreateAtValue = LocalDate.parse(minCreateDate.getText().toString(), dateFormatter).atStartOfDay();
            }

            LocalDateTime maxCreateAtValue = null;
            if (!maxCreateDate.getText().toString().equals("Конечная дата")) {
                maxCreateAtValue = LocalDate.parse(maxCreateDate.getText().toString(), dateFormatter).atTime(23, 59, 59);
            }

            ModelFilters resultModel = new ModelFilters(
                    null,
                    new ReadOnlyList<>(typesValue),
                    minWeightValue,
                    maxWeightValue,
                    minCreateAtValue,
                    maxCreateAtValue,
                    null,
                    null,
                    new ReadOnlyList<>(extensionsValue),
                    null,
                    null
            );

            IManagerOfSettings managerOfSettings = new ManagerOfSettings_Test1(this);
            managerOfSettings.saveFilters(resultModel);
            Toast.makeText(this, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show();
        });

        Button resetButton=findViewById(R.id.resetButton);
        resetButton.setOnClickListener(view -> {
            IManagerOfSettings managerOfSettings = new ManagerOfSettings_Test1(this);
            managerOfSettings.saveFilters(null);
            Toast.makeText(this,"Настройки успешно сброшены",Toast.LENGTH_SHORT).show();
        });
    }
}
