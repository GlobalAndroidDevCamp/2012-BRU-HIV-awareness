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

/**
 * @author pjv
 *
 */
public class BottomFragment extends Fragment {

	/**
	 * 
	 */
	public BottomFragment() {
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.touch, container, false);
    }


}
