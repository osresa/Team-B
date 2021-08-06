
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Data {

    private String[] currentData;
    private int currentSize;
    private float[] tm;
    private int groupSize;
    private int numChars;
    private int numGroups;

    public Data() {
    }

    public Data(String file, int groupSize) {
        String[] data = readTxt(file);

        this.currentSize = data.length;
        this.groupSize = groupSize;
        this.numChars = data[0].split(",").length - 2;
        if (currentSize % groupSize == 0) {
            this.currentData = new String[currentSize];
        } else {
            this.currentData = new String[currentSize + groupSize - (currentSize % groupSize)];
        }
        System.arraycopy(data, 0, this.currentData, 0, currentSize);
        this.normalizeToOne();
    }

    private String[] readTxt(String file) {
        ArrayList<String> tmp1 = new ArrayList();
        String[] tmp2;

        try {
            BufferedReader br = getBuffered(file);
            String line = br.readLine();
            int i = 0;
            while (line != null) {
                tmp1.add(line);
                i++;
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmp2 = new String[tmp1.size()];
        tmp2 = tmp1.toArray(tmp2);
        return tmp2;
    }

    private BufferedReader getBuffered(String link) {
        FileReader reader = null;
        BufferedReader br = null;

        try {
            File arch = new File(link);
            if (!arch.exists()) {
                System.out.println("No existe el archivo!");
            } else {
                reader = new FileReader(link);
                br = new BufferedReader(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return br;
    }

    private void normalizeToOne() { //Normaliza las características a 1 y completa faltantes con dummies
        Object[] tmp;
        float[] mins = new float[numChars];
        float[] maxs = new float[numChars];
        float data;

        tmp = dataToNumber(currentData);
        for (int i = 0; i < numChars; i++) {
            mins[i] = minArrayValue((float[]) tmp[i + 2]);
            maxs[i] = maxArrayValue((float[]) tmp[i + 2]);
        }
        for (int c = 0; c < numChars; c++) {
            for (int r = 0; r < currentSize; r++) {
                data = (((float[]) tmp[c + 2])[r] - mins[c]) / (maxs[c] - mins[c]);
                ((float[]) tmp[c + 2])[r] = data;
            }
        }
        TM(tmp);
        System.arraycopy(dataToString(tmp), 0, currentData, 0, currentSize);
        if (currentSize % groupSize != 0) {
            String dummyStr1, dummyStr2 = "";

            for (int i = 0; i < tm.length; i++) {
                dummyStr2 += tm[i] + ",";
            }
            dummyStr2 = dummyStr2.substring(0, dummyStr2.length() - 1);
            for (int i = 1; i <= groupSize - (currentSize % groupSize); i++) {
                dummyStr1 = currentSize + i + ",DUMMY" + i + ",";
                currentData[currentSize - 1 + i] = dummyStr1 + dummyStr2;
            }
            currentSize = currentData.length;
        }
        numGroups = currentSize / groupSize;
    }

    private Object[] dataToNumber(String[] data) {
        Object[] tmp = new Object[numChars + 2];
        String[] ids = new String[data.length];
        String[] names = new String[data.length];
        float[] ch;
        String[] strTmp;

        for (int i = 0; i < currentSize; i++) {
            strTmp = data[i].split(",");
            ids[i] = strTmp[0];
            names[i] = strTmp[1];
        }
        tmp[0] = ids;
        tmp[1] = names;
        for (int c = 0; c < numChars; c++) {
            ch = new float[currentSize];
            for (int r = 0; r < currentSize; r++) {
                strTmp = data[r].split(",");
                ch[r] = Float.parseFloat(strTmp[c + 2]);
            }
            tmp[c + 2] = ch;
        }
        return tmp;
    }

    private float minArrayValue(float[] array) {
        float min = array[0];

        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    private float maxArrayValue(float[] array) {
        float max = array[0];

        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private void TM(Object[] values) {
        float[] tm = new float[numChars];

        for (int i = 0; i < numChars; i++) {
            tm[i] = average((float[]) values[i + 2]);
        }
        this.tm = tm;
    }

    private float average(float[] array) {
        float sum = 0;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum / array.length;
    }

    private String[] dataToString(Object[] data) {
        String[] tmp = new String[currentSize];
        String tmpStr = "";

        for (int i = 0; i < currentSize; i++) {
            for (int j = 0; j < data.length; j++) {
                if (j < 2) {
                    tmpStr += ((String[]) data[j])[i] + ",";
                } else {
                    tmpStr += ((float[]) data[j])[i] + ",";
                }
            }
            tmp[i] = tmpStr.substring(0, tmpStr.length() - 1);
            tmpStr = "";
        }
        return tmp;
    }

    public int[][] randomGroups() {
        int[][] tmp = new int[numGroups][groupSize];
        ArrayList<Integer> itemsTmp = new ArrayList();
        int e = 0;

        for (int i = 1; i <= currentSize; i++) {
            itemsTmp.add(i);
        }
        Collections.shuffle(itemsTmp);
        for (int r = 0; r < numGroups; r++) {
            for (int c = 0; c < groupSize; c++) {
                tmp[r][c] = itemsTmp.get(e);
                e++;
            }
        }
        return tmp;
    }

    public float D(int[][] groups) {
        float[][] dm = new float[numGroups][numChars];
        float[][] im = IM(groups);
        float[] tmp = new float[numChars];;
        float sum = 0;

        for (int i = 0; i < numGroups; i++) {
            for (int j = 0; j < numChars; j++) {
                tmp[j] = (float) Math.pow(im[i][j] - tm[j], 2);
            }
            dm[i] = tmp.clone(); ////
        }
        for (int i = 0; i < dm.length; i++) {
            for (int j = 0; j < numChars; j++) {
                sum += dm[i][j];
            }
        }
        return sum;
    }

    private float[][] IM(int[][] groups) {
        float[][] im = new float[numGroups][numChars];
        int[] group = new int[groupSize];
        float[][] tmp1 = new float[groupSize][numChars];
        float[] tmp2 = new float[groupSize];;
        String[] strTmp;

        for (int i = 0; i < numGroups; i++) {
            group = groups[i];
            for (int j = 0; j < groupSize; j++) {
                strTmp = findReg(group[j]).split(",");
                for (int k = 0; k < numChars; k++) {
                    tmp1[j][k] = Float.parseFloat(strTmp[k + 2]);
                }
            }
            for (int j = 0; j < numChars; j++) {
                for (int k = 0; k < groupSize; k++) {
                    tmp2[k] = tmp1[k][j];
                }
                im[i][j] = average(tmp2);
            }
        }
        return im;
    }

    private String findReg(int id) {
        String[] tmp;
        String reg = "";

        for (int i = 0; i < currentSize; i++) {
            tmp = currentData[i].split(",");
            if (Integer.parseInt(tmp[0]) == id) {
                reg = currentData[i];
                break;
            }
        }
        return reg;
    }

    public Integer[] matrixToArray(int[][] matrix) {
        Integer[] tmp = new Integer[matrix.length * matrix[0].length];
        int idx = 0;

        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[0].length; c++) {
                tmp[idx] = matrix[r][c];
                idx++;
            }
        }
        return tmp;
    }

    public int[][] arrayToMatrix(Integer[] array, int rows, int cols) {
        int[][] tmp = new int[rows][cols];
        int idx = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tmp[r][c] = array[idx];
                idx++;
            }
        }
        return tmp;
    }

    public String[] getCurrentData() {
        return currentData;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public int getNumGroups() {
        return numGroups;
    }
}
