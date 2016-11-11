# archebot
A simple and flexible IRC bot platform

ArcheBot is designed to be usable by anyone - from novice developers who are making their first program, to seasoned professionals who need a break from the complexities of everyday life. Its purpose is to act as a bridge between the developer and the IRC server, effectively handling and delivering all types of messages that can be sent using the IRC protocol.

While other similar libraries exist out there in the big world, ArcheBot strives to implement its own unique features. A great example of this is the Command class; there's no need to handle each incoming message, check whether it should be interpreted as a command, and then determing what code to run - ArcheBot takes care of all of this already. Simply override the Command class' execute method, and register it with the internal CommandMap! Commands support a bunch of additional information, including multiple IDs, description strings, and a permission that is required to use it.

Another special thing with ArcheBot is PML, aka Property Markup Language (or maybe Perrin's Markup Language, but that seems selfish). Basically, it's a simple text file formal involving single tags and text content on a single line, which can then be converted to an easily-modifiable Java object. ArcheBot uses it to store information about its configurations, including properties and permissions.

To simplify interaction with the server, ArcheBot provides a wide variety of classes that can be used, including: Handlers, Channels, Servers, and Users.

As of ArcheBot 2.1, the most basic bot can be made in just one line of code:

new ArcheBot("ArcheBot", "irc.server.com").connect();

But, of course, additional code is encouraged!

Since ArcheBot is still in development, don't blame the developer if something doesn't work! Actually, you can, but rather than getting mad or something just send a quick email and a fix shall be found.

For more about ArcheBot, email is the best method of contact; you can also join the #archebot channel on the Esper IRC Network (irc.esper.net) for information and to see a bot or two actually working.
