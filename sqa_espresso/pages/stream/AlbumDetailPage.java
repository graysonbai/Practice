package com.kddi.android.UtaPass.sqa_espresso.pages.stream ;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.view.View;

import com.kddi.android.UtaPass.R ;
import com.kddi.android.UtaPass.sqa_espresso.common.BasicButton;
import com.kddi.android.UtaPass.sqa_espresso.common.BasicImage;
import com.kddi.android.UtaPass.sqa_espresso.common.LazyMatcher;
import com.kddi.android.UtaPass.sqa_espresso.common.LazyString;
import com.kddi.android.UtaPass.sqa_espresso.common.LineUpObject;
import com.kddi.android.UtaPass.sqa_espresso.common.StringObject;
import com.kddi.android.UtaPass.sqa_espresso.common.UtaPassUtil;
import com.kddi.android.UtaPass.sqa_espresso.common.card_behavior.IArtistName;
import com.kddi.android.UtaPass.sqa_espresso.common.card_behavior.ICover;
import com.kddi.android.UtaPass.sqa_espresso.common.card_behavior.IMyUtaButton;
import com.kddi.android.UtaPass.sqa_espresso.common.card_behavior.IPlayButton;
import com.kddi.android.UtaPass.sqa_espresso.common.card_behavior.ISongName;
import com.kddi.android.UtaPass.sqa_espresso.pages.common.BasicPage;

import org.hamcrest.Matcher;

import java.util.HashSet;
import java.util.Set;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId ;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;


public class AlbumDetailPage extends BasicPage {
    private final int SHOWMORE_BUTTON_POSITION = 12 ;
    private InternalLineUp lineUp ;

    // After swiping, structure of this page will be modified.
    // Then, there's no good generic point to verify this page is ready before and after swiping.
    // Thus, use a flag to indicate page is ready at the first time.
    private boolean readyFlag ;

    public AlbumDetailPage() {
        this.label( "AlbumPage" ) ;
    }

    public void _ready() {
        this.shuffleAllButton().assertVisible();
    }

    public BasicImage cover() {
        return new BasicImage(
                this.label() + " > cover",
                () -> withId( R.id.detail_playlist_image_normal ) ) ;
    }

    public LazyString title() {
        return new LazyString(
                this.label() + " > title",
                () -> withId( R.id.detail_editor_title ) ) ;
    }

    public BasicButton favoriteButton() {
        return new BasicButton(
                this.label() + " > favorite",
                () -> withId( R.id.detail_editor_like_layout ) ){

            public void tap(){
                super.tap();
                UtaPassUtil.sleep( 5 , "wait for action take effect" );
            }
        } ;
    }

    public LazyString description() {
        return new LazyString(
                this.label() + " > description",
                () -> withId( R.id.detail_editor_description )
        ) ;
    }

    public BasicButton playButton() {
        this.lineUp().swipeToPosition( 1 ) ;

        return new BasicButton(
                this.label() + " > play",
                () -> allOf(
                        withId( R.id.button_play_in_order ),
                        isDescendantOfA( withId( R.id.two_play_action_layout ) ) ) ){

            public LazyString text() {
                return new LazyString( this.label(), () -> allOf(
                        withClassName( endsWith( "TextView" ) ),
                        isDescendantOfA( super.matcher().execute() ) ) ) ;
            }
        } ;
    }

    public BasicButton shuffleAllButton() {
        this.lineUp().swipeToPosition( 1 ) ;

        return new BasicButton(
                this.label() + " > shuffleAll",
                () -> allOf(
                        withId( R.id.button_shuffle_play ),
                        isDescendantOfA( withId( R.id.two_play_action_layout ) ) ) ) {

            public LazyString text() {
                return new LazyString( this.label(), () -> allOf(
                        withClassName( endsWith( "TextView" ) ),
                        isDescendantOfA( super.matcher().execute() ) ) ) ;
            }
        } ;
    }

    public BasicButton showMoreButton() {
        this.lineUp().swipeToPosition( SHOWMORE_BUTTON_POSITION ) ;

        return new BasicButton(
                this.label() + " > showMore",
                () -> withId( R.id.item_detail_show_more ) ) {

            public void tap() {
                super.tap() ;
                lineUp().resetMaxIndexOfWindow() ;
            }
        } ;
    }

    public InternalLineUp lineUp() {
        if( this.description().isVisible() ) {
            this.swipeUp() ;
            this.swipeUp() ;
        }

        if( this.lineUp == null ) {
            this.lineUp = new InternalLineUp( this.label() ) ;
        }

        return this.lineUp ;
    }

    public void swipeUp() {
        onView( withId( R.id.detail_playlist_coordinator_layout ) ).perform( ViewActions.swipeUp() ) ;
    }

    public class InternalLineUp extends LineUpObject {

        public InternalLineUp(  String label ) {
            this.setMaxIndexOfLineUpObject( 25 ) ;
            this.label( label + " > LineUp" ) ;
        }

        protected Matcher<View> getMatcherToFindRecycleView() {
            return withId( R.id.detail_playlist_recycler_view ) ;
        }

        protected Matcher<View> getMatcherToCountMaxIndexOfWindow() {
            return allOf( withId( R.id.item_detail_stream_audio_layout ),
                    isCompletelyDisplayed(),
                    isDescendantOfA( this.getMatcherToFindRecycleView() ) ) ;
        }

        protected int calculateMaxIndexOfWindow() {
            int count = -1 ;
            for( int i = 0 ; i <= this.getMaxIndexOfLineUpObject(); i++ ) {
                if( ! this.isDisplayedCompletely(
                        UtaPassUtil.withIndex( this.getMatcherToCountMaxIndexOfWindow(), i ) ) ) {
                    return count ;
                }

                count++ ;
            }

            return count ;
        }

        protected int swipeToCardViewAndGetIndexOfWindow( int index ) {
            this.swipeToPosition( 1 ) ;

            if( index <= this.maxIndexFirstWindow() ) {
                return index ;
            }

            this.swipeToPosition( index + 2 ) ;
            return this.maxIndexOtherWindow() ;
        }

        public InternalCard card( int index ) {
            int indexInWindow = this.swipeToCardViewAndGetIndexOfWindow( index ) ;

            InternalCard card = new InternalCard() ;

            String label = String.format( "%s > Card(%s)",
                    this.label(),
                    index ) ;

            card.cover( label + " > Cover",
                    () -> allOf(
                            withId( R.id.item_detail_stream_audio_image ),
                            isDescendantOfA( UtaPassUtil.withIndex(
                                    this.getMatcherToCountMaxIndexOfWindow(),
                                    indexInWindow ) ) ) ) ;

            card.songName(label + " > SongName",
                    () -> allOf(
                            withId( R.id.item_detail_stream_audio_title ),
                            isDescendantOfA( UtaPassUtil.withIndex(
                                    this.getMatcherToCountMaxIndexOfWindow(),
                                    indexInWindow ) ) ) ) ;

            card.artistName(label + " > ArtistName",
                    () -> allOf(
                            withId( R.id.item_detail_stream_audio_artist ),
                            isDescendantOfA( UtaPassUtil.withIndex(
                                    this.getMatcherToCountMaxIndexOfWindow(),
                                    indexInWindow ) ) ) ) ;

            card.playButton(label + " > PlayButton",
                    () -> allOf(
                            withId( R.id.item_detail_stream_audio_myuta_register ),
                            isDescendantOfA( UtaPassUtil.withIndex(
                                    this.getMatcherToCountMaxIndexOfWindow(),
                                    indexInWindow ) ) ) ) ;

            card.myUtaButton(label + " > MyUtaButton",
                    () -> allOf(
                            withId( R.id.item_detail_stream_audio_myuta_register ),
                            isDescendantOfA( UtaPassUtil.withIndex(
                                    this.getMatcherToCountMaxIndexOfWindow(),
                                    indexInWindow ) ) ) ) ;

            return card ;
        }

        // ========================================
        // additional action
        // ========================================
        public StringObject countSongs() {
            Set<String> set = new HashSet<>() ;

            try {
                for( int i = 0 ; i <= this.getMaxIndexOfLineUpObject() ; i++ ) {
                    InternalCard card = this.card( i ) ;
                    set.add( String.format( "%s,%s",
                            card.songName().text(),
                            card.artistName().text() ) ) ;

                    this.dprint( String.format( "Fetching card(%s), SongName = %s, Artistname = %s",
                            i,
                            card.songName().text().toString(),
                            card.artistName().text().toString() ) ) ;
                }

            } catch( NoMatchingViewException e ) {
                this.dprint( e.getMessage() ) ;
            }

            StringObject strObj = new StringObject( set.size() ) ;
            strObj.label( this.label() ) ;
            return strObj ;
        }
    }

    public class InternalCard implements ICover, IPlayButton, IMyUtaButton, ISongName, IArtistName {

        String labelCover ;
        String labelSongName ;
        String labelArtistName ;
        String labelPlayButton ;
        String labelMyUtaButton ;

        private LazyMatcher matcherCover ;
        private LazyMatcher matcherSongName ;
        private LazyMatcher matcherArtistName ;
        private LazyMatcher matcherPlayButton ;
        private LazyMatcher matcherMyUtaButton ;

        public void cover( String label, LazyMatcher matcher ) {
            this.labelCover = label ;
            this.matcherCover = matcher ;
        }

        public BasicImage cover() {
            return new BasicImage( this.labelCover, this.matcherCover ) ;
        }

        public void playButton( String label, LazyMatcher matcher ) {
            this.labelPlayButton = label ;
            this.matcherPlayButton = matcher ;
        }

        public BasicButton playButton() {
            return new BasicButton( this.labelPlayButton, this.matcherPlayButton ) ;
        }

        public void myUtaButton( String label, LazyMatcher matcher ) {
            this.labelMyUtaButton = label ;
            this.matcherMyUtaButton = matcher ;
        }

        public BasicButton myUtaButton() {
            return new BasicButton( this.labelMyUtaButton, this.matcherMyUtaButton ) {
                public LazyString text() {
                    return new LazyString( this.label(), () -> allOf(
                            withClassName( endsWith( "TextView" ) ),
                            isDescendantOfA( super.matcher().execute() ) ) ) ;
                }
            } ;
        }

        public void songName( String label, LazyMatcher matcher ) {
            this.labelSongName = label;
            this.matcherSongName = matcher;
        }

        public LazyString songName() {
            return new LazyString( this.labelSongName, this.matcherSongName ) ;
        }

        public void artistName( String label, LazyMatcher matcher ) {
            this.labelArtistName = label;
            this.matcherArtistName = matcher;
        }

        public LazyString artistName() {
            return new LazyString( this.labelArtistName, this.matcherArtistName ) ;
        }

        public void tap() {
            this.cover().tap() ;
        }
    }
}




