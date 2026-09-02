## year in music
*your own album of the year*

pick a song and leave a note to represent each day, and populate your year's calendar as a journal. the calendar will look really pretty by december!  

the app will recommend you choices each day based on your recent listening history and organize your entries into a spotify playlist.   

### technicalities

built with spring boot, react, typescript, postgres, and spotify api. containerized with docker, tls certificates managed by caddy, hosted by deSEC, deployed with github actions, running on aws. crucial assists from claude code and impeccable...  

artwork credits to pinterest/[the international art glass catalogue](https://i.pinimg.com/1200x/bc/98/b6/bc98b6cfff1410269fb39304fdb6b0f6.jpg)

### demo

https://github.com/user-attachments/assets/7cbda074-3237-40f3-8d54-e8474c16cbb6

### faq

<details>
  <summary>why can't i change past entries!!</summary>
    <em>an entry is just meant to capture how you felt on that day so don't think too hard about it :') do not despair, you can edit today's entry up until the end of the 
    day.</em>
</details>

<details>
  <summary>how is this different from me curating my own playlist on spotify?</summary>
    <em>a: well i learned spring boot during the creation of this app. did i learn spring boot during the creation of spotify? methinks not. i probably wasn't even alive  
    in all seriousness, part of the website's purpose is to be a vehicle for the idea. most people probably wouldn't think to do this without knowing a service existed to 
    help people do it. also, the notetaking feature is distinct. also, here the calendar looks like a stained glass window when it's finished.</em>
</details>

<details>
  <summary>if i edit my playlist in spotify, will my calendar update?</summary>
    <em>a: errr no. first of all i don't think there's any way to force spotify to notify the app when the playlist gets edited. second, it defeats the whole "freeze the 
    past" concept. lastly if you only have one entry and you delete it, then add another song, the app has no idea which day you want to add that song to, it could be any 
    day in history</em>
</details>

*thanks for checking out my work! just a note, if you're here after 11/20/26, https://aoty.dedyn.io won't work anymore, since my 3-month free spotify premium
trial will have expired and i am too broketh to pay for an api key*
