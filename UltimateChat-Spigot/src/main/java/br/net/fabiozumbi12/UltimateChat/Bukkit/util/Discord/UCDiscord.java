package br.net.fabiozumbi12.UltimateChat.Bukkit.util.Discord;

import javax.security.auth.login.LoginException;

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;

public class UCDiscord extends ListenerAdapter {	
	private JDA jda;
	public JDA getJDA(){
		return this.jda;
	}
	
	public UCDiscord(){
		try {
			jda = new JDABuilder(AccountType.BOT).setToken("").buildBlocking();
			jda.addEventListener(this); 
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RateLimitedException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
    public void onMessageReceived(MessageReceivedEvent e) {
		
	}
}
