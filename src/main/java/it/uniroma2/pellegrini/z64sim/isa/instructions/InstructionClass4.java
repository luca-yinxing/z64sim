/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;


import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass4 extends Instruction {

    private final byte bit;
    private byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic, 4);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */

        // Set the size in memory
        this.setSize(8);

        // Will be initialized in the switch case, as well the class
        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        enc[0] = 0b01000000;

        switch(mnemonic) {
            case "clc":
                this.type = 0x00;
                this.val = 0;
                break;
            case "clp":
                this.type = 0x01;
                this.val = 0;
                break;
            case "clz":
                this.type = 0x02;
                this.val = 0;
                break;
            case "cls":
                this.type = 0x03;
                this.val = 0;
                break;
            case "cli":
                this.type = 0x04;
                this.val = 0;
                break;
            case "cld":
                this.type = 0x05;
                this.val = 0;
                break;
            case "clo":
                this.type = 0x06;
                this.val = 0;
                break;
            case "stc":
                this.type = 0x07;
                this.val = 1;
                break;
            case "stp":
                this.type = 0x08;
                this.val = 1;
                break;
            case "stz":
                this.type = 0x09;
                this.val = 1;
                break;
            case "sts":
                this.type = 0x0a;
                this.val = 1;
                break;
            case "sti":
                this.type = 0x0b;
                this.val = 1;
                break;
            case "std":
                this.type = 0x0c;
                this.val = 1;
                break;
            case "sto":
                this.type = 0x0d;
                this.val = 1;
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);

        }

        enc[0] = (byte) (enc[0] | this.type);
        this.setEncoding(enc);


    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public static String disassemble(byte[] encoding) throws DisassembleException {
        String instr = "";
        switch(encoding[0]) {
            case 0x40:
                instr += "clc";
                break;
            case 0x41:
                instr += "clp";
                break;
            case 0x42:
                instr += "clz";
                break;
            case 0x43:
                instr += "cls";
                break;
            case 0x44:
                instr += "cli";
                break;
            case 0x45:
                instr += "cld";
                break;
            case 0x46:
                instr += "clo";
                break;
            case 0x47:
                instr += "stc";
                break;
            case 0x48:
                instr += "stp";
                break;
            case 0x49:
                instr += "stz";
                break;
            case 0x4a:
                instr += "sts";
                break;
            case 0x4b:
                instr += "sti";
                break;
            case 0x4c:
                instr += "std";
                break;
            case 0x4d:
                instr += "sto"; //GHALI
                break;
            default:
                throw new DisassembleException("Unkown instruction type");
        }
        return instr;
    }
}
