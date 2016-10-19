package cn.ismartv.plugin;

/**
 * Created by huibin on 10/18/16.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.text.DecimalFormat

public class PluginImpl implements Plugin<Project> {
    void apply(Project project) {
        project.task('testTask') << {
            doTest();

            println "Hello gradle plugin"
        }
    }


    BufferedReader reader = null;
    BufferedWriter writer = null;
    String infilePath = "";
    String outfilePath = "";

    private void doTransfer(Integer width) {
        try {
            // 构造BufferedReader对象
            reader = new BufferedReader(new FileReader("/Volumes/Ismartv/Projects/GradlePluginSample/app/src/main/res/values/dimens.xml"));
            File file = new File("/Volumes/Ismartv/Projects/GradlePluginSample/app/src/main/res/values-sw" + width + "dp/dimens.xml");
            if (file.exists()) {
                file.delete();
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            file.createNewFile();

            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                bufWriter.write(convert(line, 1080 / ((float) width)));
                bufWriter.newLine();
            }
            bufWriter.flush();
            bufWriter.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String convert(String origin, Double rate) {
        origin = origin.replace("    ", "");
        if (origin.startsWith("<dimen")) {
            int startFlagIndex = origin.indexOf('>');
            int endFlagIndex = origin.lastIndexOf('<');
            String pref = origin.substring(0, startFlagIndex + 1);
            String content = origin.substring(startFlagIndex + 1, endFlagIndex);
            String unit = "dip";
            float value = 0;


            if (content.contains("dip")) {
                value = Float.parseFloat(content.replace("dip", ""));
                unit = "dip";
            } else if (content.contains("dp")) {
                value = Float.parseFloat(content.replace("dp", ""));
                unit = "dp";
            } else if (content.contains("px")) {
                value = Float.parseFloat(content.replace("px", ""));
                unit = "px";
            } else if (content.contains("sp")) {
                value = Float.parseFloat(content.replace("sp", ""));
                unit = "sp";
            }
            DecimalFormat fnum = new DecimalFormat("##0.00");
            String dd = fnum.format(value / rate);
            origin = pref + dd + unit + "</dimen>";
        }
        return origin;
    }

    public void doTest() {
        doTransfer(240);
        doTransfer(360);
        doTransfer(480);
        doTransfer(600);
        doTransfer(720);
        doTransfer(840);
        doTransfer(960);
        doTransfer(1080);
    }
}