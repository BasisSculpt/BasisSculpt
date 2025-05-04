/*
 * BSD 3-Clause License
 * 
 * Copyright (c) 2025, M. Macernis
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package basissculpt.bin;

import basissculpt.etc.ArgsGet;
import basissculpt.etc.Dic;
import java.io.*;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class Output {

    private static Output instance;
    private final PrintWriter writer;

    private Output() {
        if (!ArgsGet.getInstance().isKey("normalize")) {
            this.writer = null; // visi println bus ignoruojami
            return;
        }

        if (ArgsGet.getInstance().isKey("output")) {
            String path = ArgsGet.getInstance().getString("output");
            try {
                File file = new File(path);
                if (!file.isAbsolute()) {
                    file = new File(System.getProperty("user.dir"), path);
                }
                this.writer = new PrintWriter(new FileWriter(file));
            } catch (IOException e) {
                String msg = Dic.getMsg("ERROR_OUTPUT_FILE", path);
                throw new RuntimeException(msg, e);
            }
        } else {
            //force to use flush method
            this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
    }

    public static Output getInstance() {
        if (instance == null) {
            instance = new Output();
        }
        return instance;
    }

    public void println(Object s) {
        if (writer != null) {
            writer.println(s);
        }
    }

    public void printf(String format, Object... args) {
        if (writer != null) {
            writer.printf(format, args);
        }
    }

    public void print(String s) {
        if (writer != null) {
            writer.print(s);
        }
    }

    public void flush() {
        if (writer != null) {
            writer.flush();
        }
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
