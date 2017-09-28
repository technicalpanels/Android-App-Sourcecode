package com.example.ntd.tpapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class ConfigureRTB extends AppCompatActivity {

    /******************** View object ********************/
    private Button btnBack;

    private ToggleButton btnDriverDoorEnabled;
    private ToggleButton btnPassengerDoorEnabled;
    private ToggleButton btnSideDoorEnabled;
    private ToggleButton btnCabDoorEnabled;
    private ToggleButton btnVaultDoorEnabled;
    private ToggleButton btnRearDoorEnabled;

    private ToggleButton btnDriverDoorLockEnabled;
    private ToggleButton btnPassengerDoorLockEnabled;
    private ToggleButton btnSideDoorLockEnabled;
    private ToggleButton btnCabDoorLockEnabled;
    private ToggleButton btnVaultDoorLockEnabled;
    private ToggleButton btnRearDoorLockEnabled;

    private ToggleButton btnAlarmNetEnabled;
    private ToggleButton btnHatchEnabled;
    /*****************************************************/

    String rtb_info_str;

    RTBConfigureInfo rtb_info;

    private void initView()
    {
        btnBack = (Button)findViewById(R.id.btnBack);

        btnDriverDoorEnabled = (ToggleButton)findViewById(R.id.btnDriverDoorEnabled);
        btnPassengerDoorEnabled = (ToggleButton)findViewById(R.id.btnPassengerDoorEnabled);
        btnSideDoorEnabled = (ToggleButton)findViewById(R.id.btnSideDoorEnabled);
        btnCabDoorEnabled = (ToggleButton)findViewById(R.id.btnCabDoorEnabled);
        btnVaultDoorEnabled = (ToggleButton)findViewById(R.id.btnVaultDoorEnabled);
        btnRearDoorEnabled = (ToggleButton)findViewById(R.id.btnRearDoorEnabled);

        btnDriverDoorLockEnabled = (ToggleButton)findViewById(R.id.btnDriverDoorLockEnabled);
        btnPassengerDoorLockEnabled = (ToggleButton)findViewById(R.id.btnPassengerDoorLockEnabled);
        btnSideDoorLockEnabled = (ToggleButton)findViewById(R.id.btnSideDoorLockEnabled);
        btnCabDoorLockEnabled = (ToggleButton)findViewById(R.id.btnCabDoorLockEnabled);
        btnVaultDoorLockEnabled = (ToggleButton)findViewById(R.id.btnVaultDoorLockEnabled);
        btnRearDoorLockEnabled = (ToggleButton)findViewById(R.id.btnRearDoorLockEnabled);

        btnAlarmNetEnabled = (ToggleButton)findViewById(R.id.btnAlarmNetEnabled);
        btnHatchEnabled = (ToggleButton)findViewById(R.id.btnHatchEnabled);
    }

    private void initViewCallback()
    {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfoFollowingAllView();

                Intent ret = new Intent();

                ret.putExtra(GlobalVariable.RTB_INFO_EXTRA_RETURN_NAME, rtb_info.toString());

                setResult(RESULT_OK, ret);

                finish();
            }
        });
    }

    private void getInfoFollowingAllView()
    {
        rtb_info.setDriverDoorEnabled(btnDriverDoorEnabled.isChecked());
        rtb_info.setPassengerDoorEnabled(btnPassengerDoorEnabled.isChecked());
        rtb_info.setSideDoorEnabled(btnSideDoorEnabled.isChecked());
        rtb_info.setCabinDoorEnabled(btnCabDoorEnabled.isChecked());
        rtb_info.setVaultDoorEnabled(btnVaultDoorEnabled.isChecked());
        rtb_info.setRearDoorEnabled(btnRearDoorEnabled.isChecked());
    }

    private void setAllViewFollowingInfo()
    {
        btnDriverDoorEnabled.setChecked(rtb_info.getDriverDoorEnabled());
        btnPassengerDoorEnabled.setChecked(rtb_info.getPassengerDoorEnabled());
        btnSideDoorEnabled.setChecked(rtb_info.getSideDoorEnabled());
        btnCabDoorEnabled.setChecked(rtb_info.getCabinDoorEnabled());
        btnVaultDoorEnabled.setChecked(rtb_info.getVaultDoorEnabled());
        btnRearDoorEnabled.setChecked(rtb_info.getRearDoorEnabled());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_rtb);

        rtb_info_str = getIntent().getStringExtra(GlobalVariable.RTB_INFO_EXTRA_PASSIN_NAME);

        rtb_info = new RTBConfigureInfo();

        rtb_info.fromString(rtb_info_str);

        initView();
        initViewCallback();

        setAllViewFollowingInfo();
    }
}
