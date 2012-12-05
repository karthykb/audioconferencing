package se.ltu.M7017E.lab2.presenceserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import se.ltu.M7017E.lab2.common.messages.AnswerCall;
import se.ltu.M7017E.lab2.common.messages.Audience;
import se.ltu.M7017E.lab2.common.messages.Bye;
import se.ltu.M7017E.lab2.common.messages.Call;
import se.ltu.M7017E.lab2.common.messages.ConnectedList;
import se.ltu.M7017E.lab2.common.messages.Hello;
import se.ltu.M7017E.lab2.common.messages.Join;
import se.ltu.M7017E.lab2.common.messages.Leave;
import se.ltu.M7017E.lab2.common.messages.ListMsg;
import se.ltu.M7017E.lab2.common.messages.RoomsStart;
import se.ltu.M7017E.lab2.common.messages.RoomsStop;
import se.ltu.M7017E.lab2.common.messages.StopCall;

public class App {
	@Getter
	private Set<Client> clients = new HashSet<Client>();
	private Map<Integer, Room> rooms = new HashMap<Integer, Room>();

	public App() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(4000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			try {
				new Thread(new TCPThread(this, serverSocket.accept())).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void msg(Client client, Hello hello) {
		client.setName(hello.getName());
		clients.add(client);

		Set<String> nameClients = new HashSet<String>();
		for (Client user : clients) {
			nameClients.add(user.name);
		}
		ConnectedList cl = new ConnectedList(nameClients);

		broadcast(cl.toString());

	}

	public void msg(Client client, Bye bye) {
		// someone leaves, maybe he forgot to leave properly each room => do it
		for (Map.Entry<Integer, Room> iter : rooms.entrySet()) {
			Room room = iter.getValue();
			if (room.isWithin(client)) {
				room.left(client);
			}
		}

		// removes him from the known clients in the server
		clients.remove(client);

		Set<String> nameClients = new HashSet<String>();
		for (Client user : clients) {
			nameClients.add(user.name);
		}
		ConnectedList cl = new ConnectedList(nameClients);

		broadcast(cl.toString());

	}

	public void msg(Client client, Join join) {
		int roomId = join.getRoom();

		Room room = rooms.get(roomId);
		if (room == null) {
			// first person to enter the room => create it
			room = new Room(roomId);
			this.rooms.put(roomId, room);
		}

		room.join(client);

		// tell to the newcomer who's there
		Audience audience = new Audience();
		audience.setRoom(roomId);
		audience.setNames(room.getAudienceAsStrings());
		client.send(audience);
	}

	public void msg(Client sender, Call call) {
		Client receiver = findClientsByName(call.getReceiver());
		if (receiver != null) {
			System.out.println("sending message to " + receiver.getName());
			System.out.println(sender.getIp());
			call.setIpSender(sender.getIp());
			receiver.send(call.toString());
		} else {
			sender = findClientsByName(call.getSender());
			sender.send("ERROR," + call.getReceiver() + " is not connected :(");
		}
	}

	public void msg(StopCall stop) {
		Client client = findClientsByName(stop.getReceiver());
		client.send(stop.toString());
	}

	public void msg(Client answerer, AnswerCall answer) {
		Client requester = findClientsByName(answer.getSender());
		System.out.println(answer.toString());
		System.out.println("sending message to" + requester.getName());
		answer.setIpReceiver(answerer.getIp());
		requester.send(answer.toString());
	}

	public void msg(Client client, Leave leave) {
		// removes him properly from the room
		this.rooms.get(leave.getRoom()).left(client);
	}

	public void msg(Client client, ListMsg list) {
		client.send(new RoomsStart());

		// for each room (key = id, value = room)
		for (Map.Entry<Integer, Room> entry : rooms.entrySet()) {
			Audience audience = new Audience();
			audience.setRoom(entry.getKey());
			audience.setNames(entry.getValue().getAudienceAsStrings());

			client.send(audience);
		}

		client.send(new RoomsStop());
	}

	public void broadcast(String message) {
		for (Client client : clients) {
			client.send(message);
		}
	}

	public Client findClientsByName(String name) {
		System.out.println("name to search " + name + "<");
		Client clientToReturn = null;
		System.out.println("number of clients" + clients.size());
		for (Client client : clients) {
			System.out.println("client name : " + client.getName());
			if (client.getName().equals(name)) {
				clientToReturn = client;
			}
		}
		return clientToReturn;
	}

}