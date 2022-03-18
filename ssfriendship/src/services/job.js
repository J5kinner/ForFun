/** @license 4050 Boyz
  * Copyright (c) 4050 Boyz, Inc. and its affiliates.
  *
  * Authors: 
  * 
  */
import axios from 'axios'

const baseURL = "/api/"

/**
 * Insert a new job into the database.
 * @param JSON job
 * @returns {promise} 
 */
const addJob = (job, token) => {
    const config = {headers: {Authorization: "bearer " + token}}
    // console.log("JOB IMAGES: ", job.images)
    return axios.post(baseURL + 'new-favour', 
        {
            "title": job.title,
            "description": job.description,
            "cost": job.cost,
            "city": job.city,
            "streetAddress": job.streetAddress,
            "lat": job.lat,
            "long": job.long,
            "images": job.images
        }, config
    ).then(response => response.data).catch(e => e.response.data)
}

const delFavour = (favour, token) => {
    const config = {headers: {Authorization: "bearer " + token}}
    axios.delete(baseURL + "favours/" + favour._id, config)
    .then(response => {
        console.log(response)
    }).catch(e => null)
}

const editFavour = (job, token) => {
    const config = {headers: {Authorization: "bearer " + token}}
    axios.put(baseURL + "favours/"+ job._id, job, config)
    .then(response => response.data).catch(e => null)
}

const getFavours = () => {
    return axios.get(baseURL)
}

const getFavour = (id) => {
    return axios.get(baseURL + "favours/" + id)
}

const cancelFavour = (favour , token) => {
    const config = {headers: {Authorization: "bearer " + token}}
    axios.post(baseURL + "favours/cancel/" + favour._id, favour, config)
    .then(response =>response.data)
    .catch(e => null)
}

const acceptFavour = (favour , token) => {
    const config = {headers: {authorization: "bearer " + token}}
    axios.post(baseURL + "favours/accept/" + favour._id , favour , config)
    .then(response => {
        response.data() 
    })
    .catch(e => null)
}

const approveFavour = (favour, token, operator) => {
    // console.log(operator)
    const config = {headers: {authorization: "bearer " + token}}
    axios.post(baseURL + "favours/approve/" + favour._id, {operator: operator}, config)
    .then(response => {
        response.data() 
    })
    .catch(e => null)
}

const uploadImageForFavour = (formData) => {
    axios.post('upload', formData, {headers: {'Content-Type': 'multipart/form-data'}})
    .then(response =>{ alert(response.data)})
}

const completeFavour = (favour , token) => {
    const config = {headers: {authorization: "bearer " + token}}
    axios.post(baseURL + "favours/complete/" + favour._id , favour,  config)
    .then(response => {
        response.data() 
    })
    .catch(e => null)
}

const exportedObject = {
    addJob,
    delFavour,
    editFavour,
    getFavours,
    getFavour,
    cancelFavour,
    acceptFavour,
    approveFavour,
    uploadImageForFavour,
    completeFavour
};

export default exportedObject;