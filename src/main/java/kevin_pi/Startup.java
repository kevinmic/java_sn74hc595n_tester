package kevin_pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Startup {
    public static final Pin D_DATA_DS = RaspiPin.GPIO_00;
    public static final Pin O_OE = RaspiPin.GPIO_01;
    public static final Pin T_STCP = RaspiPin.GPIO_02;
    public static final Pin H_SHCP = RaspiPin.GPIO_03;
    public static final Pin R_MR = RaspiPin.GPIO_04;
    public static final int SLEEP = 200;

    final GpioController gpio;
    private final OutPin data_ser;
    private final OutPin oe;
    private final OutPin t_stcp_rclk;
    private final OutPin h_shcp_srclk;
    private final OutPin r_mr_srclr;


    public Startup() {
        this.gpio = GpioFactory.getInstance();
        data_ser = new OutPin(gpio, D_DATA_DS, "d");
        oe = new OutPin(gpio, O_OE, "o");
        t_stcp_rclk = new OutPin(gpio, T_STCP, "t");
        h_shcp_srclk = new OutPin(gpio, H_SHCP, "h");
        r_mr_srclr = new OutPin(gpio, R_MR, "m");
    }

    public static void main(String[] args) throws InterruptedException {
        new Startup().run();
    }

    private void run() {
        System.out.println("PIN:" + data_ser.getPin().getName() + " names: data,SER -- dD");
        System.out.println("PIN:" + oe.getPin().getName() + " names: OE -- oO");
        System.out.println("PIN:" + t_stcp_rclk.getPin().getName() + " names: STCP,RCLK -- tT");
        System.out.println("PIN:" + h_shcp_srclk.getPin().getName() + " names: SHCP,SRCLK -- hH");
        System.out.println("PIN:" + r_mr_srclr.getPin().getName() + " names: MR,SRCLR -- mM");


        String line = null;
        while (true) {
            System.out.println("PREV COMMAND - " + line);
            System.out.println("YOUR WISH IS MY COMMAND [tThHoOmM10]:");
             new ArrayList<>();
            String newLine = readLine();
            if (newLine.trim().length() == 0) {
                newLine = line;
            }
            line = newLine;
            List<Runnable> runnables = toCommandList(line.replaceAll("\\s",""));

            for (Runnable runnable : runnables) {
                runnable.run();
            }

        }
    }

    private List<Runnable> toCommandList(String command) {
        List<Runnable> callables = new ArrayList<>();
        if (command != null) {
            if (command.startsWith("p")) {
                callables.addAll(doPattern(command));
            }
            else {
                for (int i = 0; i < command.length(); i++) {
                    callables.add(toCommand(command.substring(i, i + 1)));
                }
            }
        }
        return callables;
    }

    private Collection<? extends Runnable> doPattern(String command) {
        List<Runnable> callables = new ArrayList<>();

        Matcher matcher = Pattern.compile("p(([hHtToOmMdDw]+):)+(\\d+)").matcher(command.trim());

        if (matcher.matches()) {
            int count = Integer.parseInt(matcher.group(3));

            String[] pats = command.substring(1).split(":");
            List<Runnable> preCallables = new ArrayList<>();
            for (int i = 0; i < pats.length - 1; i++ ) {
                preCallables.addAll(toCommandList(pats[i]));
            }

            for (int i = 0; i < count; i++) {
                callables.addAll(preCallables);
            }
        }
        else {
            System.out.println("COULDNT MATCH [" + command + "]");
        }

        return callables;
    }

    private Runnable toCommand(String command) {
        Boolean hiLo = null;
        OutPin outPin = null;
        switch (command) {
            case "D":
            case "O":
            case "T":
            case "H":
            case "M":
                hiLo = true;
                break;
            case "d":
            case "o":
            case "t":
            case "h":
            case "m":
                hiLo = false;
                break;
        }

        switch (command) {
            case "d":
            case "D":
                outPin = data_ser;
                break;
            case "O":
            case "o":
                outPin = oe;
                break;
            case "T":
            case "t":
                outPin = t_stcp_rclk;
                break;
            case "H":
            case "h":
                outPin = h_shcp_srclk;
                break;
            case "M":
            case "m":
                outPin = r_mr_srclr;
        }

        if (outPin != null && hiLo != null) {
            final boolean bit = hiLo;
            final OutPin pin = outPin;
            return new Runnable() {
                @Override
                public void run() {
                    System.out.println(command + "-" + (bit?1:0));
                    if (bit) {
                        pin.getOutput().high();
                    } else {
                        pin.getOutput().low();
                    }
                }
            };
        }
        else if ("w".equals(command)) {
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

        }
        System.out.println("NOT RECOGNIZED - " + command);
        return null;

    }

    private OutPin getPin(String command) {
        switch(command) {
            case "D":
                return data_ser;
            case "O":
                return oe;
            case "T":
                return t_stcp_rclk;
            case "H":
                return h_shcp_srclk;
            case "R":
                return r_mr_srclr;

            default:
                System.out.println("INVALID COMMAND - " + command);
                return null;
        }

    }

    private String readLine() {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
