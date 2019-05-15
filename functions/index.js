const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const cors = require('cors')({origin: true});
admin.initializeApp();

/**
* Here we're using Gmail to send 
*/
let transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'blogglierapp@gmail.com',
        pass: 'boggliestest435'
    }
});

admin.initializeApp()


exports.mailGameLog = functions.database.ref('/Boards/{HostCode}/inSession/gameOver').onUpdate((change, context) => {
    cors(req, res, () => {
      	req = null;
        const mailOptions = {
            from: 'bogglier boggliest <blogglierapp@gmail.com>', // Something like: Jane Doe <janedoe@gmail.com>
            to: 'sergiupocol7@gmail.com',
            subject: 'Boggler Game Log', // email subject
            html: `<p style="font-size: 16px;">Pickle Riiiiiiiiiiiiiiiick!!</p>
                <br />
                <img src="https://images.prod.meredith.com/product/fc8754735c8a9b4aebb786278e7265a5/1538025388228/l/rick-and-morty-pickle-rick-sticker" />`
             // email content in HTML
        };
  
        // returning result
        return transporter.sendMail(mailOptions, (erro, info) => {
            if(erro){
            	console.log(erro.toString());
                return res.send(erro.toString());
            }
            return res.send('Complete');
        });
    });   

    return null;
});
