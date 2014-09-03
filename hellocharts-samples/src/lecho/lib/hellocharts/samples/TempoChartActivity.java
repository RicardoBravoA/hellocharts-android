package lecho.lib.hellocharts.samples;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SimpleValueFormatter;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.Utils;
import lecho.lib.hellocharts.view.LineChartView;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TempoChartActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tempo_chart);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	public static class PlaceholderFragment extends Fragment {

		private LineChartView chart;
		private LineChartData data;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_tempo_chart, container, false);

			chart = (LineChartView) rootView.findViewById(R.id.chart);

			generateDefaultData();

			return rootView;
		}

		private void generateDefaultData() {
			// I got speed in range (0-50) and height in meters in range(200 - 300). I want this chart to display both
			// information. Differences between speed and height values are large and chart doesn't look good so I need
			// to modify height values to be in range of speed values.

			float maxSpeed = 50;
			float minSpeed = 0;
			float minHeight = 200;
			float maxHeight = 300;
			float maxTempo = 2;// 2 minutes per kilometer
			float minTempo = 6; // 6 minutes per kilometer

			float heightToSpeedScale = maxSpeed / maxHeight;
			float sub = (minHeight * heightToSpeedScale) / 2;

			int numValues = 50;

			Line line;
			List<PointValue> values;
			List<Line> lines = new ArrayList<Line>();

			// Height line
			values = new ArrayList<PointValue>();
			for (int i = 0; i < numValues; ++i) {
				// Some random height values, add +200 to make line a little more natural
				float rawHeight = (float) (Math.random() * 100 + 200);
				float normalizedHeight = rawHeight * heightToSpeedScale - sub;
				values.add(new PointValue(i, normalizedHeight));
			}

			line = new Line(values);
			line.setColor(Color.GRAY);
			line.setHasPoints(false);
			line.setFilled(true);
			line.setStrokeWidth(1);
			lines.add(line);

			// Speed line
			values = new ArrayList<PointValue>();
			for (int i = 0; i < numValues; ++i) {
				// Some random speed values, add +20 to make line a little more natural.
				values.add(new PointValue(i, (float) Math.random() * 30 + 20));
			}

			line = new Line(values);
			line.setColor(Utils.COLOR_GREEN);
			line.setHasPoints(false);
			line.setStrokeWidth(3);
			lines.add(line);

			data = new LineChartData(lines);
			// Distance axis(bottom X) with formatter that will ad [km] to values, remember to modify max label charts
			// value.
			Axis distanceAxis = new Axis();
			distanceAxis.setName("Distance");
			distanceAxis.setTextColor(Utils.COLOR_ORANGE);
			distanceAxis.setMaxLabelChars(4);
			distanceAxis.setFormatter(new SimpleValueFormatter(0, null, "km".toCharArray()));
			data.setAxisXBottom(distanceAxis);

			// Speed axis
			data.setAxisYLeft(new Axis().setName("Speed [km/h]").setHasLines(true).setMaxLabelChars(3)
					.setTextColor(Utils.COLOR_GREEN));

			// Height axis, this axis need custom formatter that will translate values back to real height values.
			data.setAxisYRight(new Axis().setName("Height [m]").setMaxLabelChars(3)
					.setFormatter(new HeightValueFormater(heightToSpeedScale, sub, 0, null, null)));

			// Set data
			chart.setLineChartData(data);

			// Important: adjust viewport, you could skip this step but in this case it will looks better with custom
			// viewport. Set
			// viewport with Y range 0-55;
			Viewport v = chart.getMaxViewport();
			v.set(v.left, 55, v.right, 0);
			chart.setMaxViewport(v);
			chart.setViewport(v, false);

		}

		private static class HeightValueFormater extends SimpleValueFormatter {

			private float scale;
			private float sub;

			public HeightValueFormater(float scale, float sub, int digits, char[] prependedText, char[] apendedText) {
				super(digits, prependedText, apendedText);
				this.scale = scale;
				this.sub = sub;
			}

			@Override
			public int formatValue(char[] formattedValue, float[] values, char[] label) {
				int index = values.length - 1;// I just need last value from this array because SimpleValueFormatter
				// uses only last value and that is enough for axis labels.
				values[index] = (values[index] + sub) / scale;
				return super.formatValue(formattedValue, values, label);
			}

			@Override
			public int formatValue(char[] formattedValue, float[] values, char[] label, int digits) {
				int index = values.length - 1;// I just need last value from this array because SimpleValueFormatter
				// uses only last value and that is enough for axis labels.
				values[index] = (values[index] + sub) / scale;
				return super.formatValue(formattedValue, values, label, digits);
			}
		}
	}
}
