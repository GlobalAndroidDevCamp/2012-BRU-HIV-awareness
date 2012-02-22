/**
 * 
 */
package net.lp.hivawareness.v4;

import net.lp.hivawareness.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

/**
 * @author pjv
 *
 */
public class StartFragment extends Fragment {

	private Button button;
	private Spinner spinnerGender;
	private Spinner spinnerRegion;

	/**
	 * 
	 */
	public StartFragment() {
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.start, container, false);
        
		button = (Button) view.findViewById(R.id.button1);
		button.setOnClickListener((View.OnClickListener)getActivity());

		spinnerGender = (Spinner) view.findViewById(R.id.spinner_gender);
		spinnerGender.setOnItemSelectedListener((OnItemSelectedListener)getActivity());

		spinnerRegion = (Spinner) view.findViewById(R.id.spinner_region);
		spinnerRegion.setOnItemSelectedListener((OnItemSelectedListener)getActivity());
		
		return view;
    }

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		button.setOnClickListener(null);
		button=null;

		spinnerGender.setOnItemSelectedListener(null);
		spinnerGender=null;

		spinnerRegion.setOnItemSelectedListener(null);
		spinnerRegion=null;
	}

    
    
}
