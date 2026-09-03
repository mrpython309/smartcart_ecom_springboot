const axios = require('axios');
axios.get('http://54.236.219.71:3000/api/ai/search?query=book')
  .then(res => console.log(JSON.stringify(res.data, null, 2)))
  .catch(err => {
    console.log(err.response ? err.response.status : err.message);
    if(err.response && err.response.data) console.log(err.response.data);
  });
