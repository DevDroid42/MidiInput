package midiSerial;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Midi {
	SerialCom serial = new SerialCom("CH340");

	public Midi() {
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(infos[i]);
				// does the device have any transmitters?
				// if it does, add it to the device list
				System.out.println(infos[i]);

				// get all transmitters
				List<Transmitter> transmitters = device.getTransmitters();
				// and for each transmitter

				for (int j = 0; j < transmitters.size(); j++) {
					// create a new receiver
					transmitters.get(j).setReceiver(
							// using my own MidiInputReceiver
							new MidiInputReceiver(device.getDeviceInfo().toString()));
				}

				Transmitter trans = device.getTransmitter();
				trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));

				// open each device
				device.open();
				// if code gets this far without throwing an exception
				// print a success message
				System.out.println(device.getDeviceInfo() + " Was Opened");

			} catch (MidiUnavailableException e) {
			}
		}

	}

//tried to write my own class. I thought the send method handles an MidiEvents sent to it
	public class MidiInputReceiver implements Receiver {
		public String name;
		List<NoteData> notes = new ArrayList<NoteData>();
		boolean arp = false;

		public MidiInputReceiver(String name) {
			this.name = name;
		}

		public void send(MidiMessage msg, long timeStamp) {
			if (msg.getStatus() == 144) {

				// Print to confirm signal arrival
				System.out.println("midi received");

				byte[] aMsg = msg.getMessage();
				// take the MidiMessage msg and store it in a byte array

				// msg.getLength() returns the length of the message in bytes
				for (int i = 0; i < msg.getLength(); i++) {
					System.out.println(aMsg[i]);
					// aMsg[0] channel
					// aMsg[1] is the note value as an int. This is the important one.
					// aMsg[2] velocity
				}
				// if the note is pressed add to list
				if (aMsg[2] != 0) {
					notes.add(new NoteData(aMsg[1], aMsg[2]));
				} else {
					//if there is only 1 note held down stop it before removing
					if (notes.size() == 1) {
						byte[] data = { notes.get(notes.size() - 1).note, 0};
						sendNote(data);
						removeNote(aMsg[1]);
					}else {
						removeNote(aMsg[1]);
					}
				}
				
				//if there are notes held down send the data for the last note pressed
				if(notes.size() > 0) {
					byte[] data = { notes.get(notes.size() - 1).note, notes.get(notes.size() - 1).velocity };
					sendNote(data);
				}
				System.out.println(notes.size());
			}
		}
		
		public void arp() {
			
		}

		public void close() {
		}
		
		public void sendNote(byte[] data) {
			try {
				serial.sendData(data);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// removes note at index
		public void removeNote(byte index) {
			for (int i = 0; i < notes.size(); i++) {
				if (notes.get(i).note == index) {
					notes.remove(i);
					break;
				}
			}
		}
	}
}

class NoteData {
	public NoteData(byte note, byte velocity) {
		this.note = note;
		this.velocity = velocity;
	}

	public byte note = 0;
	public byte velocity = 0;
}