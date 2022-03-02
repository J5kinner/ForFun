import React from 'react'
import { connect } from 'react-redux'
import 'src/assets/css/tab.css'
import mechanicus from '../assets/images/adeptus-mechanicus.svg'

export const Tabs = (props) => {
  return (
    <div className="tab"><img src="mechanicus"/></div>
  )
}

const mapStateToProps = (state) => ({})

const mapDispatchToProps = {}

export default connect(mapStateToProps, mapDispatchToProps)(Tabs)