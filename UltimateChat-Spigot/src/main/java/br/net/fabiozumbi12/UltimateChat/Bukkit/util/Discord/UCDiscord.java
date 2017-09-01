package br.net.fabiozumbi12.UltimateChat.Bukkit.util.Discord;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class UCDiscord extends ListenerAdapter {	
	private JDA jda;
	public JDA getJDA(){
		return this.jda;
	}
	
	public UCDiscord(){
		try {
			jda = new JDABuilder(AccountType.BOT).setToken("").buildBlocking();
			jda.addEventListener(this); 
		} catch (LoginException | IllegalArgumentException
				| InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
    public void onMessageReceived(MessageReceivedEvent e) {
		
	}
}
