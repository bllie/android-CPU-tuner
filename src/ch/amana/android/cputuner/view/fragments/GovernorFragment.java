package ch.amana.android.cputuner.view.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.model.IGovernorModel;

public class GovernorFragment extends GovernorBaseFragment {

	private TextView tvExplainGov;
	private TextView labelGovThreshUp;
	private TextView labelGovThreshDown;
	private EditText etGovTreshUp;
	private EditText etGovTreshDown;
	private Spinner spinnerSetGov;
	private EditText etScript;
	private LinearLayout llFragmentTop;
	private String[] availCpuGovs;
	private String origThreshUp;
	private String origThreshDown;
	private SeekBar sbPowersaveBias;

	public GovernorFragment(GovernorFragmentCallback callback, IGovernorModel governor) {
		super(callback, governor);
		origThreshUp = governor.getGovernorThresholdUp() + "";
		origThreshDown = governor.getGovernorThresholdDown() + "";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.governor_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		SettingsStorage settings = SettingsStorage.getInstance();
		FragmentActivity act = getActivity();

		availCpuGovs = CpuHandler.getInstance().getAvailCpuGov();

		llFragmentTop = (LinearLayout) act.findViewById(R.id.llGovernorFragment);
		tvExplainGov = (TextView) act.findViewById(R.id.tvExplainGov);
		labelGovThreshUp = (TextView) act.findViewById(R.id.labelGovThreshUp);
		labelGovThreshDown = (TextView) act.findViewById(R.id.labelGovThreshDown);
		etGovTreshUp = (EditText) act.findViewById(R.id.etGovTreshUp);
		etGovTreshDown = (EditText) act.findViewById(R.id.etGovTreshDown);
		spinnerSetGov = (Spinner) act.findViewById(R.id.SpinnerCpuGov);
		etScript = (EditText) act.findViewById(R.id.etScript);
		sbPowersaveBias = (SeekBar) act.findViewById(R.id.sbPowersaveBias);

		if (!settings.isEnableScriptOnProfileChange()) {
			llFragmentTop.removeView(act.findViewById(R.id.llScript));
		}

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_item, availCpuGovs);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				callback.updateModel();
				String gov = parent.getItemAtPosition(pos).toString();
				getGovernorModel().setGov(gov);
				callback.updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				callback.updateView();
			}

		});

		OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {


			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && etGovTreshUp.getVisibility() == View.VISIBLE) {
					String upthresh = etGovTreshUp.getText().toString();
					String downthresh = etGovTreshDown.getText().toString();
					try {
						int up = Integer.parseInt(upthresh);
						int down = 0;
						if (etGovTreshDown.getVisibility() == View.VISIBLE) {
							down = Integer.parseInt(downthresh);
						}
						if (up > 100 || up < 0) {
							Toast.makeText(getActivity(), R.string.msg_up_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshUp.setText(origThreshUp);
						}
						if (down > 100 || down < 0) {
							Toast.makeText(getActivity(), R.string.msg_down_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshDown.setText(origThreshDown);
						}
						if (up > down) {
							// all OK
							return;
						}
						Toast.makeText(getActivity(), R.string.msg_up_threshhold_smaler_than_the_down_threshold, Toast.LENGTH_LONG).show();
						down = up - 10;
						etGovTreshDown.setText(down + "");
					} catch (Exception e) {
						Toast.makeText(getActivity(), R.string.msg_threshhold_NaN, Toast.LENGTH_LONG).show();
					}
				}

			}
		};
		etGovTreshUp.setOnFocusChangeListener(onFocusChangeListener);
		etGovTreshDown.setOnFocusChangeListener(onFocusChangeListener);
	}

	@Override
	public void updateModel() {
		IGovernorModel governorModel = getGovernorModel();
		governorModel.setGovernorThresholdUp(etGovTreshUp.getText().toString());
		governorModel.setGovernorThresholdDown(etGovTreshDown.getText().toString());
		if (SettingsStorage.getInstance().isEnableScriptOnProfileChange()) {
			governorModel.setScript(etScript.getText().toString());
		} else {
			governorModel.setScript("");
		}
		governorModel.setPowersaveBias(sbPowersaveBias.getProgress());
	}

	@Override
	public void updateView() {
		String curGov = getGovernorModel().getGov();
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}
		tvExplainGov.setText(GuiUtils.getExplainGovernor(getActivity(), curGov));
		if (SettingsStorage.getInstance().isPowerUser()) {
			etScript.setText(getGovernorModel().getScript());
		}
		sbPowersaveBias.setProgress(getGovernorModel().getPowersaveBias());
		updateGovernorFeatures();
	}

	private void updateGovernorFeatures() {
		String gov = getGovernorModel().getGov();

		boolean hasThreshholdUpFeature = true;
		boolean hasThreshholdDownFeature = true;

		if (CpuHandler.GOV_POWERSAVE.equals(gov) || CpuHandler.GOV_PERFORMANCE.equals(gov) || CpuHandler.GOV_USERSPACE.equals(gov) || CpuHandler.GOV_INTERACTIVE.equals(gov)) {
			hasThreshholdUpFeature = false;
			hasThreshholdDownFeature = false;
		} else if (CpuHandler.GOV_ONDEMAND.equals(gov)) {
			hasThreshholdDownFeature = false;
		}

		int up = getGovernorModel().getGovernorThresholdUp();
		int down = getGovernorModel().getGovernorThresholdDown();
		if (hasThreshholdUpFeature) {
			labelGovThreshUp.setVisibility(View.VISIBLE);
			etGovTreshUp.setVisibility(View.VISIBLE);
			if (up < 2) {
				up = 90;
			}
			etGovTreshUp.setText(up + "");
		} else {
			getGovernorModel().setGovernorThresholdUp(0);
			etGovTreshUp.setText("");
			labelGovThreshUp.setVisibility(View.INVISIBLE);
			etGovTreshUp.setVisibility(View.INVISIBLE);
		}

		if (hasThreshholdDownFeature) {
			labelGovThreshDown.setVisibility(View.VISIBLE);
			etGovTreshDown.setVisibility(View.VISIBLE);
			if (down >= up || down < 1) {
				if (up > 30) {
					down = up - 10;
				} else {
					down = up - 1;
				}
			}
			etGovTreshDown.setText(down + "");
		} else {
			getGovernorModel().setGovernorThresholdDown(0);
			etGovTreshDown.setText("");
			labelGovThreshDown.setVisibility(View.INVISIBLE);
			etGovTreshDown.setVisibility(View.INVISIBLE);
		}

	}
}