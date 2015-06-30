# The Toastd
An Android app to facilitate bluetooth communication between Android devices.

Andrew Garner, June 30, 2015


This app will eventually be forked into two different systems.
First, the Toastd. Inspired by the Internet of Things, I am designing an Android and embedded systems solution to the age old question, "Is my toast done?" By connecting the power cord of a toaster to a device which is then plugged into a power outlet, current is measured as a toaster is toasting. When the current stops, your toast is done. At that point, utilizing a bluetooth connection (or perhaps WiFi direct, or even over the internet), the device will notify the user that the toast is ready to eat. Though it is a solution to an incredibly trivial problem, it is a good model for interactions between interconnected devices around the home, and is in the spirit of IoT.

The second system will be a bluetooth chat client. It will allow multiple people within close proximity to chat. A particular device will act as a server, and other connections will go through it to chat with the other connected clients. But, when a certain number of clients connect, the system will switch to a ring topology. So instead of one device spreading a thin connection between many clients, each client will connect in such a way that minimizes the number of connections per client but still connects everyone. A webserver could even be used, along with location data, to seamlessly connect many people who are in close proximity, and to even amplifify the effective local range if needed.

---------------------------------------------------------

The app demonstrates key technologies such as (but not limited to) handlers, services, asynctasks, receivers, Bluetooth sockets and connections, and adapters.
