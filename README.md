This bot was created for the one small company that sells beers and cigarettes in Russia. The owner of this company asked me to create online shop on the base of telegram bot in order to increase amount of sellings.
Customer technical task was consisted of these main points:
  1) implement the main menu with 6 sections - catalog, bucket, orders, about, settings, support
  2) organise the database which will includes several tables: users, orders, products
  3) calculate cost of delivery relying on location and totoal cost of order

The backend of the bot is writing with Java Spring Boot framework due to its suitable concept of architecture for restful standalone apps. At first, I've chosen MongoDB as the data base for this project, but later rewrite code for the default JPA model that connects to PlanetScale (cloud platform). To calculate the cost of delivery, I used the Yandex Map API to determine in which price zone users order delivery. Two years ago the application worked on the Heroku server, but due to the cancellation of the free tariff plan, now the bot is spinning on Google Cloud ;)
### If you want to test and get familiar with the bot tap on [the link](https://t.me/PivoIDudki_bot) or find it in telegram @PivoIDudki_bot
