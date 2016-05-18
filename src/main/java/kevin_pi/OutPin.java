package kevin_pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

public class OutPin {
    private final com.pi4j.io.gpio.Pin pin;
    private final GpioPinDigitalOutput output;
    private final String name;

    public OutPin(GpioController gpio, Pin pin, String name) {
        this.pin = pin;
        this.output = gpio.provisionDigitalOutputPin(pin);
        this.name = name;
    }

    public com.pi4j.io.gpio.Pin getPin() {
        return pin;
    }

    public GpioPinDigitalOutput getOutput() {
        return output;
    }

    public String getName() {
        return name;
    }
}
