package ioio.examples.stepper_textview;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton button_;
	private ToggleButton toggleButton1_;
	private TextView textView1_;
	private int StepCount;
	private Handler stepHandler_;
	private Runnable mUpdate;
	private Thread myThread;
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button);
		toggleButton1_ = (ToggleButton) findViewById(R.id.toggleButton1);
		textView1_ = (TextView)findViewById(R.id.textView);
		StepCount = 0;
		stepHandler_ = new Handler();
		stepHandler_.post(mUpdate);
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/**
		 * The on-board LED.
		 */
		private DigitalOutput led_;
		private DigitalOutput IN1_;
		private DigitalOutput IN2_;
		private DigitalOutput IN3_;
		private DigitalOutput IN4_;


		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 *
		 * @throws ConnectionLostException When IOIO connection is lost.
		 * @see ioio.lib.util.IOIOLooper#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			showVersions(ioio_, "IOIO connected!");
			led_ = ioio_.openDigitalOutput(0, false);
			IN1_ = ioio_.openDigitalOutput(2, false);
			IN2_ = ioio_.openDigitalOutput(3, false);
			IN3_ = ioio_.openDigitalOutput(4, false);
			IN4_ = ioio_.openDigitalOutput(5, false);
			enableUi(true);

		}

		/**
		 * Called repetitively while the IOIO is connected.
		 *
		 * @throws ConnectionLostException When IOIO connection is lost.
		 * @throws InterruptedException    When the IOIO thread has been interrupted.
		 * @see ioio.lib.util.IOIOLooper#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			led_.write(!button_.isChecked());

			toggleButton1_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					/*mUpdate = new Runnable(){
						public void run(){
							textView1_.setText(""+StepCount);
							stepHandler_.postDelayed(this, 10);
							stepHandler_.post(mUpdate);
						}
					};*/
					mUpdate = new Runnable() {
						@Override
						public void run() {
							while (StepCount < 500) {
								textView1_.post(new Runnable() {
									@Override
									public void run() {
										textView1_.setText(""+StepCount);
									}
								});
								try {
									IN1_.write(true);
									IN2_.write(false);
									IN3_.write(true);
									IN4_.write(false);
									StepCount++;
									try {
										Thread.sleep(20);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									IN1_.write(false);
									IN2_.write(true);
									IN3_.write(true);
									IN4_.write(false);
									StepCount++;
									try {
										Thread.sleep(20);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									IN1_.write(false);
									IN2_.write(true);
									IN3_.write(false);
									IN4_.write(true);
									StepCount++;
									try {
										Thread.sleep(20);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									IN1_.write(true);
									IN2_.write(false);
									IN3_.write(false);
									IN4_.write(true);
									StepCount++;
									try {
										Thread.sleep(20);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if (StepCount >= 500) {
										IN1_.write(false);
										IN2_.write(false);
										IN3_.write(false);
										IN4_.write(false);
										StepCount = 0;
										break;
									}
								} catch (ConnectionLostException e) {
									e.printStackTrace();
								}
							}
						}
					};
					myThread = new Thread(mUpdate);
					myThread.start();
				}
			});
			Thread.sleep(10);
		}

		/**
		 * Called when the IOIO is disconnected.
		 *
		 * @see ioio.lib.util.IOIOLooper#disconnected()
		 */
		@Override
		public void disconnected() {
			enableUi(false);
			toast("IOIO disconnected");
		}

		/**
		 * Called when the IOIO is connected, but has an incompatible firmware version.
		 *
		 * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
		 */
		@Override
		public void incompatible() {
			showVersions(ioio_, "Incompatible firmware version!");
		}
	}

	/**
	 * A method to create our IOIO thread.
	 *
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void showVersions(IOIO ioio, String title) {
		toast(String.format("%s\n" +
						"IOIOLib: %s\n" +
						"Application firmware: %s\n" +
						"Bootloader firmware: %s\n" +
						"Hardware: %s",
				title,
				ioio.getImplVersion(VersionType.IOIOLIB_VER),
				ioio.getImplVersion(VersionType.APP_FIRMWARE_VER),
				ioio.getImplVersion(VersionType.BOOTLOADER_VER),
				ioio.getImplVersion(VersionType.HARDWARE_VER)));
	}

	private void toast(final String message) {
		final Context context = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private int numConnected_ = 0;

	private void enableUi(final boolean enable) {
		// This is slightly trickier than expected to support a multi-IOIO use-case.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (enable) {
					if (numConnected_++ == 0) {
						button_.setEnabled(true);
					}
				} else {
					if (--numConnected_ == 0) {
						button_.setEnabled(false);
					}
				}
			}
		});
	}
}