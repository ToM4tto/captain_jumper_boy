package com.example.captainjumperboy.engine

import android.graphics.Canvas
import com.example.captainjumperboy.engine.component.Component
import com.example.captainjumperboy.math.Collision
import com.example.captainjumperboy.ui.GameView


interface OnCollidedListener {
    fun onCollided(file: GameObject)
}
open class Scene(var view: GameView) {
    private var gameObjectList = ArrayList<GameObject>()

    private var listener: OnCollidedListener? = null

    init {
        Assets.view = view
    }

    fun registerListener(listener: OnCollidedListener)//call function in gameobject
    {
        this.listener = listener
    }
    //get first inactive object, creates a new game object if none found
    fun createObject(name : String = "") : GameObject{
        return try {
            val gameObject = gameObjectList.first { gameObject -> !gameObject.active }
            gameObject.name = name

            gameObject
        }
        catch (_ : Exception){
            val gameObject = GameObject()
            gameObject.name = name
            if (!gameObjectList.add(gameObject))
                throw Exception()

            gameObjectList.last()
        }
    }

    //same as createObject, but calls start()
    fun spawnObject() : GameObject{
        val gameObject = createObject()
        gameObject.start()
        return gameObject
    }

    fun findObject(name : String) : GameObject{
        return gameObjectList.first{gameObject -> gameObject.name == name }
    }

    fun startEarly(){
        Component.scene = this
        for (i in 0 until gameObjectList.count()){
            gameObjectList[i].startEarly()
        }
    }

    fun start(){
        Component.scene = this
        for (i in 0 until gameObjectList.count()){
            gameObjectList[i].start()
        }
    }

    fun update(){
        gameObjectList.forEach {gameObject ->  gameObject.update()}

        //collision loop..need to destroy platforms out of viewport otherwise this will get slower..
        gameObjectList.forEach {gameObject ->
            if(gameObject.hasComponent<Collision.AABB>())
            {
                gameObjectList.forEach{gameObject2 ->
                    if(gameObject.name!=gameObject2.name && gameObject2.hasComponent<Collision.AABB>())//if does not equal itself and both objects has aabb,do checks
                    {
                        val aabb=gameObject.getComponent<Collision.AABB>()?:return
                        val aabb2=gameObject2.getComponent<Collision.AABB>()?:return
                        if(aabb.collidesWith(aabb2))
                        {
                            listener?.onCollided(gameObject2)
                        }
                        else if(aabb2.collidesWith(aabb)){
                            listener?.onCollided(gameObject)
                        }

                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas){
        gameObjectList.forEach {gameObject ->  gameObject.draw(canvas)}
    }
}