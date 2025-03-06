package com.example;


import com.sun.tools.attach.VirtualMachine;

public class AttachDemo {

    public static void main(String[] args) throws Exception {
        String pid = "33028";
        String agentPath = "D:\\Home\\chauncy_utils\\Utils\\Agent\\target\\Agent-1.0.jar";

        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentPath);

        vm.detach();
        System.out.println("Agent injected success.");
    }
}
