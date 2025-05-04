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
package basissculpt.lib.read;

import basissculpt.bin.LogFile;
import basissculpt.etc.ArgsGet;
import basissculpt.etc.Dic;
import java.io.*;
import java.util.*;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class BasisParser {

    public static class Block {

        public final String type;         // e.g. "S" or "SP"
        public final String header;       // e.g. "2 1.00"
        public final List<String> lines;  // other lines

        public Block(String type, String header) {
            this.type = type;
            this.header = header;
            this.lines = new ArrayList<>();
        }
    }

    public static class AtomSection {

        public final String atom;
        public final List<Block> blocks = new ArrayList<>();

        public AtomSection(String atom) {
            this.atom = atom;
        }
    }

    public static List<AtomSection> parse(File file) throws IOException {
        List<AtomSection> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            AtomSection currentAtom = null;
            Block currentBlock = null;
            boolean endblock = true;
            int linenum =0;

            while ((line = reader.readLine()) != null) {
                linenum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("!")) {
                    continue; // comment at the begining 
                }

                //if (line.startsWith("-")) {
                //    currentAtom = new AtomSection(line.substring(1).trim());
                // "-" used by gbs standart but in input is without it.
                // Updated:
                if (line.startsWith("-") || (endblock && Character.isLetter(line.charAt(0)))) {
                    String atomName = line.startsWith("-") ? line.substring(1).trim() : line.trim();
                    currentAtom = new AtomSection(atomName);
                    endblock = false;

                    if (ArgsGet.getInstance().isKey("verbose")) {
                        if (!line.startsWith("-")) {
                            LogFile.getInstance().println(Dic.getMsg("WARN_NON_GBS_INPUT"));
                            LogFile.getInstance().println("Line "+linenum + "> " + line);
                        }
                    }
                    result.add(currentAtom);
                    continue;
                }

                if (line.equals("****")) {
                    currentBlock = null;
                    endblock = true;
                    continue;
                }

                if (Character.isLetter(line.charAt(0))) {
                    // Starts new block
                    String[] parts = line.split("\\s+");
                    currentBlock = new Block(parts[0], line.substring(parts[0].length()).trim());
                    if (currentAtom != null) {
                        currentAtom.blocks.add(currentBlock);
                    }
                    continue;
                }

                if (currentBlock != null) {
                    currentBlock.lines.add(line);
                }
            }
        }

        if (result.isEmpty()) {
            String msg = Dic.getMsg("ERROR_INPUT_FILE_ATOMS", file.getPath());
            System.out.println(msg);
            System.exit(0);
        }

        return result;
    }

    // Subblock splitting, e. g. SP into S and P
    public static Map<String, List<String>> expandBlock(Block block) {
        Map<String, List<String>> expanded = new LinkedHashMap<>();
        String[] letters = block.type.split("");

        for (String l : letters) {
            expanded.put(l, new ArrayList<>());
        }

        for (String line : block.lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length != letters.length + 1) {
                continue; // problems
            }
            String exponent = parts[0];
            for (int i = 0; i < letters.length; i++) {
                String l = letters[i];
                expanded.get(l).add(exponent + " " + parts[i + 1]);
            }
        }

        return expanded;
    }
}
