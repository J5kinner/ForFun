import React from 'react'
import { connect } from 'react-redux'

export const Generator = (props) => {
  return (
    <div>Generator</div>
  )
}

const mapStateToProps = (state) => ({})

const mapDispatchToProps = {}

export default connect(mapStateToProps, mapDispatchToProps)(Generator)